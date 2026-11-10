# Persistence compatibility fixture

`legacy-campus-v1.bin` was produced by the original acceptance suite before the November 2026 package refactor. It contains only fictional demo/test identities, their documented demo password derivations, and completed test competitions. It is not a user's database.

TC-69 loads this fixture with the current repository and verifies the account count and published knockout champion. Keeping `edu.aau.ccms.Model` and its nested serialized record names preserves compatibility. Arena maps the legacy format to a competition type and tournament format in memory; the file is updated to schema two only on the next successful save.

`legacy-active-v1.bin` was generated on 10 November 2026 by compiling the unchanged pre-Arena `Model`, `DemoData`, and `Passwords` sources from the verified working-copy backup. It contains the original 16 fictional accounts, five teams, and three seeded events, including an active four-team knockout bracket with no recorded results. AR-26 loads these actual version-one serialized bytes, completes the bracket through Arena's service, and publishes the champion. This is a deliberately generated compatibility test fixture, not historical user data.
