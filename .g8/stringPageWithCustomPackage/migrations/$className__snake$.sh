#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:taxYear/$className;format="decap"$                        controllers.$packageName$.$className$Controller.onPageLoad(taxYear:Int, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:taxYear/$className;format="decap"$                        controllers.$packageName$.$className$Controller.onSubmit(taxYear:Int, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:taxYear/change$className$                  controllers.$packageName$.$className$Controller.onPageLoad(taxYear:Int, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:taxYear/change$className$                  controllers.$packageName$.$className$Controller.onSubmit(taxYear:Int, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$className;format="decap"$.title = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.heading = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.checkYourAnswersLabel = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.error.required = Enter $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.error.length = $className$ must be $maxLength$ characters or less" >> ../conf/messages.en
echo "$className;format="decap"$.change.hidden = $className$" >> ../conf/messages.en

echo "Migration $className;format="snake"$ completed"
