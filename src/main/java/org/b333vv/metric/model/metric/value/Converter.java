/*
 * Copyright 2020 b333vv
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

package org.b333vv.metric.model.metric.value;

import org.jscience.mathematics.number.LargeInteger;
import org.jscience.mathematics.number.Rational;
import org.jscience.mathematics.number.Real;

class Converter {
    private Converter(){
//        Only static
    }

    public static Rational toRational(LargeInteger i) {
        return Rational.valueOf(i, LargeInteger.ONE);
    }

    public static Real toReal(LargeInteger value) {
        return Real.valueOf(value, 0, 0);
    }

    public static Real toReal(Rational value) {
        Real dividendReal = Real.valueOf(value.getDividend(), 0, 0);
        Real divisorReal = Real.valueOf(value.getDivisor(), 0, 0);
        return dividendReal.divide(divisorReal);
    }
}
