/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms.behaviours

import play.api.data.{Form, FormError}

trait CurrencyFieldBehaviours extends FieldBehaviours {

  def currencyField(
    form: Form[_],
    fieldName: String,
    nonNumericError: FormError,
    twoDecimalPlacesError: FormError,
    defaultFields: (String, String)*
  ): Unit = {

    "not bind non-numeric numbers" in {

      forAll(nonNumerics -> "nonNumeric") { nonNumeric =>
        val result = form.bind(Map(fieldName -> nonNumeric) concat defaultFields).apply(fieldName)
        result.errors must contain only nonNumericError
      }
    }

    "not bind decimals that are not two decimal places" in {

      forAll(decimalsNotTwoDecimalPlaces -> "decimal") { decimal =>
        val result = form.bind(Map(fieldName -> decimal) concat defaultFields).apply(fieldName)
        result.errors must contain only twoDecimalPlacesError
      }
    }
  }

  def currencyFieldWithRange(
    form: Form[_],
    fieldName: String,
    minimum: Int,
    maximum: Int,
    expectedError: FormError,
    defaultFields: (String, String)*
  ): Unit = {

    s"not bind decimals outside under $minimum" in {
      val result = form.bind(Map(fieldName -> (minimum - 1).toString) concat defaultFields).apply(fieldName)
      result.errors must contain only expectedError
    }

    s"not bind decimals outside above $maximum" in {
      val result = form.bind(Map(fieldName -> (maximum + 1).toString) concat defaultFields).apply(fieldName)
      result.errors must contain only expectedError
    }
  }

  def currencyFieldWithRange(
    form: Form[_],
    fieldName: String,
    minimum: BigDecimal,
    maximum: BigDecimal,
    expectedError: FormError,
    defaultFields: (String, String)*
  ): Unit = {

    s"not bind decimals outside under $minimum" in {
      val result = form.bind(Map(fieldName -> (minimum - 1).toString) concat defaultFields).apply(fieldName)
      result.errors must contain only expectedError
    }

    s"not bind decimals outside above $maximum" in {
      val result = form.bind(Map(fieldName -> (maximum + 1).toString) concat defaultFields).apply(fieldName)
      result.errors must contain only expectedError
    }
  }

  def currencyFieldWithMaximum(
    form: Form[_],
    fieldName: String,
    maximum: Int,
    expectedError: FormError,
    defaultFields: (String, String)*
  ): Unit =
    s"not bind integers above $maximum" in {

      forAll(intsAboveValue(maximum) -> "intAboveMax") { number: Int =>
        val result = form.bind(Map(fieldName -> number.toString) concat defaultFields).apply(fieldName)
        result.errors must contain only expectedError
      }
    }

  def currencyFieldWithMaximum(form: Form[_], fieldName: String, maximum: BigDecimal, expectedError: FormError): Unit =
    s"not bind BigDecimals above $maximum" in {

      forAll(bigDecimalsAboveValue(maximum) -> "intAboveMax") { number: BigDecimal =>
        val result = form.bind(Map(fieldName -> number.toString)).apply(fieldName)
        result.errors must contain only expectedError
      }
    }

  def currencyFieldWithMinimum(
    form: Form[_],
    fieldName: String,
    minimum: Int,
    expectedError: FormError,
    defaultFields: (String, String)*
  ): Unit = {

    s"not bind integers below $minimum" in {

      forAll(intsBelowValue(minimum) -> "intBelowMin") { number: Int =>
        val result = form.bind(Map(fieldName -> number.toString) concat defaultFields).apply(fieldName)
        result.errors must contain only expectedError
      }
    }

    def currencyFieldWithRange(
      form: Form[_],
      fieldName: String,
      minimum: BigDecimal,
      maximum: BigDecimal,
      expectedError: FormError,
      defaultFields: (String, String)*
    ): Unit = {

      s"not bind BigDecimals outside under $minimum" in {
        val result = form.bind(Map(fieldName -> (minimum - 1).toString) concat defaultFields).apply(fieldName)
        result.errors must contain only expectedError
      }

      s"not bind BigDecimals outside above $maximum" in {
        val result = form.bind(Map(fieldName -> (maximum + 1).toString) concat defaultFields).apply(fieldName)
        result.errors must contain only expectedError
      }
    }

    def currencyFieldWithMinimum(
      form: Form[_],
      fieldName: String,
      minimum: BigDecimal,
      expectedError: FormError
    ): Unit =
      s"not bind BigDecimals below $minimum" in {

        forAll(bigDecimalsBelowValue(minimum) -> "intBelowMin") { number: BigDecimal =>
          val result = form.bind(Map(fieldName -> number.toString)).apply(fieldName)
          result.errors must contain only expectedError
        }
      }
  }
}
