
# income-tax-property-frontend

This is where we make API calls from users viewing and making changes to the Property section of their income tax return.

## Running the service locally

To run the service locally, ensure that the following dependencies are installed and properly configured:

- Rancher/Docker: Follow the installation guide on HMRC confluence
- MongoDB: Follow the [MongoDB](https://docs.mongodb.com/manual/installation/) installation guide to install and set up MongoDB being used by HMRC at the time
- Service Manager: Install/configure Service Manager 2 [sm2](https://github.com/hmrc/sm2) to manage and run the service locally.

The service manager version 2 profile for this service is:

    sm2 --start INCOME_TAX_PROPERTY_FRONTEND

This service runs on port: `localhost:19161`

Start MongoDB (if it isn't already running):

    docker run --restart unless-stopped -d -p 27017-27019:27017-27019 --name mongodb mongo:4.2

To start the additional required services locally:

    sm2 --start INCOME_TAX_SUBMISSION_ALL

To run locally stop the service manager service for frontend:

    sm2 --stop INCOME_TAX_PROPERTY_FRONTEND
    ./run.sh **OR** sbt -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes run

To test the branch you're working on locally. You will need to run `sm2 --stop INCOME_TAX_PROPERTY_FRONTEND` followed by
`./run.sh`

### Running Tests

- Run Unit Tests:  `sbt test`
- Run Integration Tests: `sbt it/test`
- Run Unit and Integration Tests: `sbt test it/test`
- Run Unit and Integration Tests with coverage report: `./check.sh`<br/>
  which runs `sbt clean coverage test it/test coverageReport dependencyUpdates`

### Feature Switches

| Feature                         | Description                                                         |
|---------------------------------|---------------------------------------------------------------------|
| Welsh Language                  | Enables a toggle to allow the user to change language to/from Welsh |
| sessionCookieServiceEnabled     | Retrieves session data from V&C when enabled                        |

## Using the service

There are two main flows:

* Agent sign up
* Individual sign up

### Local

#### Individual
* Login via: [http://localhost:9949/auth-login-stub/gg-sign-in](http://localhost:9949/auth-login-stub/gg-sign-in)
* Entry page: [http://localhost:9302/update-and-submit-income-tax-return/2025/start](http://localhost:9302/update-and-submit-income-tax-return/2025/start)

| Enrolment Key | Identifier Name | Identifier Value |
|---------------|-----------------|------------------|
| HMRC-MTD-IT   | MTDITID         | 1234567890       |


### Agent 
* Login via: [http://localhost:9949/auth-login-stub/gg-sign-in](http://localhost:9949/auth-login-stub/gg-sign-in)
* Entry page : [http://localhost:9302/update-and-submit-income-tax-return/test-only/2024/additional-parameters?ClientNino=AC180000A&ClientMTDID=1234567890](http://localhost:9302/update-and-submit-income-tax-return/test-only/2024/additional-parameters?ClientNino=AC180000A&ClientMTDID=1234567890)

| Enrolment Key  | Identifier Name      | Identifier Value	 |
|----------------|----------------------|-------------------|
| HMRC-MTD-IT    | MTDITID              | 1234567890        |
| HMRC-AS-AGENT  | AgentReferenceNumber | XARN1234567       |

### Staging

*Requires HMRC VPN*

#### Individual
* Login via: [https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in](https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in)
* Entry page : [http://localhost:9302/update-and-submit-income-tax-return/test-only/2024/additional-parameters?ClientNino=AC180000A&ClientMTDID=1234567890](http://localhost:9302/update-and-submit-income-tax-return/test-only/2024/additional-parameters?ClientNino=AC180000A&ClientMTDID=1234567890)

| Enrolment Key | Identifier Name | Identifier Value |
|---------------|-----------------|------------------|
| HMRC-MTD-IT   | MTDITID         | 1234567890       |

#### Agent
* Login via: [https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in](https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in)
* Entry page : [http://localhost:9302/update-and-submit-income-tax-return/test-only/2024/additional-parameters?ClientNino=AC180000A&ClientMTDID=1234567890](http://localhost:9302/update-and-submit-income-tax-return/test-only/2024/additional-parameters?ClientNino=AC180000A&ClientMTDID=1234567890)

| Enrolment Key  | Identifier Name      | Identifier Value	 |
|----------------|----------------------|-------------------|
| HMRC-MTD-IT    | MTDITID              | 1234567890        |
| HMRC-AS-AGENT  | AgentReferenceNumber | XARN1234567       |


## Testing the service

* Run unit tests: `sbt clean test`
* Run integration tests: `sbt clean it/test`
* Run performance tests: provided in the repo [income-tax-submission-performance-tests](https://github.com/hmrc/income-tax-submission-performance-tests)
* Run acceptance tests: provided in the repo [income-tax-submission-journey-tests](https://github.com/hmrc/income-tax-submission-journey-tests)

## Ninos with stub data for Property in Staging Environment

| Nino      | Description           |
|-----------|-----------------------|
| AC210000B | Traditional (accrual) |
| AC210000A | Cash                  |
| AC180000A | Traditional (accrual) | 
| AC190000B | Cash                  |

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
