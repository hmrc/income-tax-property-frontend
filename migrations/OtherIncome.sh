#!/bin/bash

echo ""
echo "Applying migration OtherIncome"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /otherIncome                  controllers.OtherIncomeController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /otherIncome                  controllers.OtherIncomeController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeOtherIncome                        controllers.OtherIncomeController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeOtherIncome                        controllers.OtherIncomeController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "otherIncome.title = OtherIncome" >> ../conf/messages.en
echo "otherIncome.heading = OtherIncome" >> ../conf/messages.en
echo "otherIncome.checkYourAnswersLabel = OtherIncome" >> ../conf/messages.en
echo "otherIncome.error.nonNumeric = Enter your otherIncome using numbers" >> ../conf/messages.en
echo "otherIncome.error.required = Enter your otherIncome" >> ../conf/messages.en
echo "otherIncome.error.wholeNumber = Enter your otherIncome using whole numbers" >> ../conf/messages.en
echo "otherIncome.error.outOfRange = OtherIncome must be between {0} and {1}" >> ../conf/messages.en
echo "otherIncome.change.hidden = OtherIncome" >> ../conf/messages.en

echo "Migration OtherIncome completed"
