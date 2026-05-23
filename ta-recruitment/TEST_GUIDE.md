# TA Recruitment Project Test Guide

## 1. Purpose

This project has added a complete test suite covering models, permission checks, and file storage logic. It helps verify core business behaviors and quickly detect regressions.

## 2. Test Scope

### Model Layer
- `User`: username generation, password matching, role permissions, status text
- `Job`: field read/write, default status, constructor behavior
- `Application`: field read/write, status enumeration, workload field
- `TAProfile`: resume field read/write, GPA null value, skill list
- `AuditLogEntry`: constructor and read/write

### Permission Checks
- `PermissionChecker`: access control for `TA`, `MO`, `ADMIN` to `/secure/*` resources

### Storage Layer
- `UserStorage`: account creation, update, duplicate email protection
- `ProfileStorage`: TA profile saving and loading
- `JobStorage`: job creation, query, archiving, update, exception data fault tolerance
- `ApplicationStorage`: application creation, retrieval, status update, batch update, expiration handling
- `AuditLogStorage`: audit log writing, query, operator/type lists
- `JobHistoryStorage`: job history record writing, snapshot query, lookup by ID

## 3. Running Tests

Execute in the project root directory:

```bash
cd d:\桌面\Software-Engineering-Group-Assignment-main\ta-recruitment
mvn test
```

## 4. New Test File Locations

- `src/test/java/com/bupt/ta/model/`
- `src/test/java/com/bupt/ta/security/`
- `src/test/java/com/bupt/ta/storage/`
- `src/test/java/com/bupt/ta/TestUtils.java`

## 5. Current Test Results

- Tests executed: 51
- Failures: 0
- Errors: 0
- Skipped: 0

## 6. Further Extension Suggestions

If you wish to further extend test coverage, consider:
- Adding Servlet layer integration tests to simulate HTTP requests
- Writing end-to-end UI tests covering JSP page behaviors
- Supplementing management function path tests for `AdminDashboard`, `UserManagement`, etc.

## 7. All Test Cases (classes and test methods)

Below is a complete list of the test classes currently in the project and the `@Test` methods they contain. Use this as the authoritative test-case inventory.

- `com.bupt.ta.model.UserTest`
	- `buildDefaultUsernameFromEmail`
	- `passwordMatchesWorksCorrectly`
	- `rolePermissionOrderingIsCorrect`
	- `getStatusTextReturnsActiveOrDisabled`

- `com.bupt.ta.model.JobTest`
	- `defaultStatusIsOpenAfterConstructor`
	- `settersAndGettersRoundtrip`
	- `statusConstantsAreDistinct`
	- `updatedAtDefaultsToPostedAtWhenConstructed`

- `com.bupt.ta.model.TAProfileTest`
	- `profileFieldsRoundtrip`
	- `emptySkillsListIsInitialized`
	- `canReplaceSkillsList`
	- `gpaIsNullable`

- `com.bupt.ta.model.ApplicationModelTest`
	- `applicationFieldsRoundtrip`
	- `canSetAndClearAssignedWorkloadHours`
	- `statusEnumContainsExpectedValues`
	- `canMutateFields`

- `com.bupt.ta.model.AuditLogEntryTest`
	- `constructorSetsRequiredFields`
	- `settersAndGettersWork`
	- `idIsUniqueForDifferentEntries`

- `com.bupt.ta.security.PermissionCheckerTest`
	- `adminCanAccessAllSecureResources`
	- `moCanAccessMoAndTaResourcesOnly`
	- `taCanAccessTaResourcesOnly`
	- `utilityMethodsRespectRoleHierarchy`

- `com.bupt.ta.storage.UserStorageTest`
	- `createAndFindUser`
	- `updateUserPersistsChanges`
	- `createUserWithDuplicateEmailThrows`

- `com.bupt.ta.storage.ProfileStorageTest`
	- `saveAndLoadProfileByEmail`
	- `savingNullProfileIsIgnored`
	- `savingProfileWithoutEmailIsIgnored`
	- `loadReturnsNullForUnknownEmail`

- `com.bupt.ta.storage.JobStorageTest`
	- `createNewJobPersistsAndCanBeFound`
	- `findAllReturnsSavedJobs`
	- `archiveJobUpdatesStatusToArchived`
	- `updateReplacesExistingJob`
	- `malformedJobLinesAreSkipped`
	- `jobWithInvalidDatesIsSkipped`

- `com.bupt.ta.storage.ApplicationStorageTest`
	- `createNewApplicationPersistsAndCanBeFound`
	- `findByTaEmailIsCaseInsensitive`
	- `hasAppliedReturnsFalseForNullInputs`
	- `updateStatusChangesApplicationStatuses`
	- `bulkUpdateStatusUpdatesMultipleApplications`
	- `updateAssignedWorkloadStoresValue`
	- `updateAssignedWorkloadWithUnknownIdDoesNothing`
	- `expiredApplicationsAreAutomaticallyMarkedExpired`

- `com.bupt.ta.storage.AuditLogStorageTest`
	- `addAndQueryAuditEntries`
	- `findOperatorsAndActionTypesReturnSortedUniqueValues`
	- `queryWithDateRangeFiltersResults`

- `com.bupt.ta.storage.JobHistoryStorageTest`
	- `recordWithoutSnapshotPersistedAndCanBeQueried`
	- `recordWithSnapshotCanReturnLastSnapshot`
	- `findByJobIdAndChangedAtReturnsCorrectEntry`
	- `findByJobIdAndChangedAtReturnsNullWhenMissing`

## 8. How to Run Individual Tests

You can run all tests or a specific class/method using Maven.

- Run all tests:

```bash
mvn test
```

- Run a specific test class:

```bash
mvn -Dtest=ClassNameTest test
```

Example:

```bash
mvn -Dtest=com.bupt.ta.storage.JobStorageTest test
```

- Run a single test method in a class:

```bash
mvn -Dtest=ClassNameTest#methodName test
```

Example:

```bash
mvn -Dtest=com.bupt.ta.storage.ApplicationStorageTest#createNewApplicationPersistsAndCanBeFound test
```

## 9. Notes

- Tests that interact with file-based storage create temporary directories during execution and clean them up in `@AfterEach` methods.
- If you add new tests, place them under `src/test/java` following the existing package layout so Maven picks them up automatically.
