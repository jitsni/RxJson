/**
 * Copyright 2013-2014 Jitendra Kotamraju.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jitsni.rx.json;

import rx.Observable;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author Jitendra Kotamraju
 */
public class JsonTest {
    /*

{
     "firstName": "John",
     "lastName": "Smith",
     "age": 25,
     "address": {
         "streetAddress": "21 2nd Street",
         "city": "New York",
         "state": "NY",
         "postalCode": "10021"
     },
     "phoneNumber": [
         {
           "type": "home",
           "number": "212 555-1234"
         },
         {
           "type": "fax",
           "number": "646 555-4567"
         }
     ]
}

     */

    public static void main(String ... args) throws Exception {
        List<CharBuffer> buffers = new ArrayList<>();
        CharBuffer buffer = CharBuffer.allocate(1);
        try (Reader wikiReader = new InputStreamReader(JsonTest.class.getResourceAsStream("/wiki.json"), UTF_8)) {
            while (wikiReader.read(buffer) > 0) {
                buffer.flip();
                buffers.add(buffer);
                buffer = CharBuffer.allocate(1);
            }
        }
        System.out.println(buffers);

        Observable.from(buffers)
                .compose(new JsonTokenTransformer())
                .compose(new JsonValueTransformer())
                .forEach(jsonValue -> System.out.println("action1 buffer = " + jsonValue));

    }

}
