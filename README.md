
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

## Ninos with stub data for Property in Staging Environment

| Nino      | Description           |
|-----------|-----------------------|
| AC210000B | Traditional (accrual) |
| AC210000A | Cash                  |
| AC180000A | Traditional (accrual) | 
| AC190000B | Cash                  |

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").