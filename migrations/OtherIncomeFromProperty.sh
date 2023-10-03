#!/bin/bash

echo ""
echo "Applying migration OtherIncomeFromProperty"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /otherIncomeFromProperty                  controllers.OtherIncomeFromPropertyController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /otherIncomeFromProperty                  controllers.OtherIncomeFromPropertyController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeOtherIncomeFromProperty                        controllers.OtherIncomeFromPropertyController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeOtherIncomeFromProperty                        controllers.OtherIncomeFromPropertyController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "otherIncomeFromProperty.title = OtherIncomeFromProperty" >> ../conf/messages.en
echo "otherIncomeFromProperty.heading = OtherIncomeFromProperty" >> ../conf/messages.en
echo "otherIncomeFromProperty.checkYourAnswersLabel = OtherIncomeFromProperty" >> ../conf/messages.en
echo "otherIncomeFromProperty.error.nonNumeric = Enter your otherIncomeFromProperty using numbers" >> ../conf/messages.en
echo "otherIncomeFromProperty.error.required = Enter your otherIncomeFromProperty" >> ../conf/messages.en
echo "otherIncomeFromProperty.error.wholeNumber = Enter your otherIncomeFromProperty using whole numbers" >> ../conf/messages.en
echo "otherIncomeFromProperty.error.outOfRange = OtherIncomeFromProperty must be between {0} and {1}" >> ../conf/messages.en
echo "otherIncomeFromProperty.change.hidden = OtherIncomeFromProperty" >> ../conf/messages.en

echo "Migration OtherIncomeFromProperty completed"
