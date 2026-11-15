const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate() {
  return new Date(Date.now() - 2000);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- Unauthenticated rejections ---
test("Unauthenticated user cannot read or write user profiles, notes, or course_progress", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("notes").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("course_progress").get()
  );
});

// --- User Profile ---
test("Authenticated owner can create and update their own UserProfile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastDate();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      learnerName: "طالب EduTech",
      xpPoints: 150,
      streakDays: 2,
      completedLessons: 1,
      preferredTheme: "SYSTEM",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      xpPoints: 200,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("UserProfile rejects cross-user access and shadow/ghost fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastDate();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      learnerName: "Alice",
      xpPoints: 100,
      streakDays: 1,
      completedLessons: 0,
      preferredTheme: "DARK",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Cross-user read & write fail
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      xpPoints: 9999,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Shadow/ghost field injection fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Immutable createdAt tamper fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 10000),
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

// --- Notes Subcollection ---
test("Owner can CRUD notes; cross-user listing and reading are blocked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastDate();
  const noteRef = aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_1");

  await assertSucceeds(
    noteRef.set({
      userId: ALICE_UID,
      localNoteId: 1,
      title: "ملخص الذكاء الاصطناعي",
      content: "أهم أفكار الدرس الأول",
      tag: "ذكاء اصطناعي",
      accentKey: "cyan",
      isPinned: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("notes").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("notes").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").get()
  );

  await assertSucceeds(
    noteRef.update({
      title: "ملخص محدّث",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertSucceeds(noteRef.delete());
});

// --- Course Progress Subcollection ---
test("Owner can create/update/list course_progress; invalid progressPercent is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastDate();
  const progRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("course_progress")
    .doc("course_1");

  await assertSucceeds(
    progRef.set({
      userId: ALICE_UID,
      courseTitle: "منهجية عقلية التعليم والتكنولوجيا",
      category: "القيادة الرقمية",
      progressPercent: 60,
      isCompleted: false,
      isBookmarked: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Out of range progress rejected
  await assertFails(
    progRef.update({
      progressPercent: 150,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Cross-user read rejected
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("course_progress").get()
  );
});
