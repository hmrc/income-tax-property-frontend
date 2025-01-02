#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/$packageName$.routes"

echo "" >> ../conf/$packageName$.routes
echo "GET        /:taxYear/$className;format="decap"$                        controllers.$packageName$.$className$Controller.onPageLoad(taxYear:Int, mode: Mode = NormalMode)" >> ../conf/$packageName$.routes
echo "POST       /:taxYear/$className;format="decap"$                        controllers.$packageName$.$className$Controller.onSubmit(taxYear:Int, mode: Mode = NormalMode)" >> ../conf/$packageName$.routes

echo "GET        /:taxYear/change$className$                  controllers.$packageName$.$className$Controller.onPageLoad(taxYear:Int, mode: Mode = CheckMode)" >> ../conf/$packageName$.routes
echo "POST       /:taxYear/change$className$                  controllers.$packageName$.$className$Controller.onSubmit(taxYear:Int, mode: Mode = CheckMode)" >> ../conf/$packageName$.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$className;format="decap"$.title = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.heading = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.checkYourAnswersLabel = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.error.required = Select yes if $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.change.hidden = $className$" >> ../conf/messages.en

echo "Migration $className;format="snake"$ completed"
