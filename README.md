
# income-tax-property-frontend

This is where we make API calls from users viewing and making changes to the Property section of their income tax return.

## Running the service locally

You will need to have the following:
- Installed/configured [service-manager](https://github.com/hmrc/service-manager)

The service manager profile for this service is:

    sm2 --start INCOME_TAX_PROPERTY_FRONTEND

This service runs on port: `localhost:19161`

Run the following command to start the remaining services locally:

    sudo mongod (If not already running)
    sm2 --start INCOME_TAX_SUBMISSION_ALL

To run the service locally:

    sudo mongod (If not already running)
    sm2 --start INCOME_TAX_SUBMISSION_ALL
    sm2 --stop INCOME_TAX_PROPERTY_FRONTEND
    ./run.sh **OR** sbt -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes run

## Ninos with stub data for Property

### In-Year
| Nino            | Description                             |
|-----------------|-----------------------------------------|
| <to_be_defined> | properties that make this nino relevant | 

### End of Year
| Nino            | Description                             |
|-----------------|-----------------------------------------|
| <to_be_defined> | properties that make this nino relevant | 

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").