# Persistence compatibility fixture

`legacy-campus-v1.bin` was produced by the original acceptance suite before the November 2026 package refactor. It contains only fictional demo/test identities, their documented demo password derivations, and completed test competitions. It is not a user's database.

TC-69 loads this fixture with the current repository and verifies the account count and published knockout champion. Keeping `edu.aau.ccms.Model` and its nested serialized record names preserves compatibility without silently migrating or discarding existing data.
