###ai-content-generator (@ReddReelStoryz)

To run tests:
Ensure -Dspring.profiles.active={env},test -Ptest set when running mvn commands
where {env} is one of the following: dev/prod, all required environment variables are set.

-Ptest allows integration tests to run
-Dspring.profiles.active=test ensures that certain configuration is not loaded (e.g. Async, Scheduling) as this
will cause tests to fail (logic is tested, not Springs ability to schedule or run methods asynchronously).

-Dspring.profiles.active={env} simply loads the correct environment variables for the environment you are running in.
