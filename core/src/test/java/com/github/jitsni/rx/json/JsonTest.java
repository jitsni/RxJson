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

import org.junit.Test;
import org.mockito.InOrder;
import rx.Observable;
import rx.Observer;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.only;

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

    @Test
    public void testWiki() throws Exception {
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

        Observable<JsonValue> values = Observable.from(buffers)
                .compose(new JsonTokenTransformer())
                .compose(new JsonValueTransformer());
                //.forEach(jsonValue -> System.out.println("action1 buffer = " + jsonValue));
        verifyWikiValues(values);
    }

    private void verifyWikiValues(Observable<JsonValue> tokens) {
        JsonObject address = new JsonObject();
        address.add("streetAddress", new JsonString("21 2nd Street"));
        address.add("city", new JsonString("New York"));
        address.add("state", new JsonString("NY"));
        address.add("postalCode", new JsonString("10021"));

        JsonObject home = new JsonObject();
        home.add("type", new JsonString("home"));
        home.add("number", new JsonString("212 555-1234"));
        JsonObject fax = new JsonObject();
        fax.add("type", new JsonString("fax"));
        fax.add("number", new JsonString("646 555-4567"));

        JsonArray phone = new JsonArray();
        phone.add(home);
        phone.add(fax);

        JsonObject wiki = new JsonObject();
        wiki.add("firstName", new JsonString("John"));
        wiki.add("lastName", new JsonString("Smith"));
        wiki.add("age", new JsonNumber("25"));
        wiki.add("address", address);
        wiki.add("phoneNumber", phone);

        System.out.println("wiki = " + wiki);

        @SuppressWarnings("unchecked")
        Observer<JsonValue> observer = mock(Observer.class);
        tokens.subscribe(observer);
        verify(observer, times(1)).onNext(wiki);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onCompleted();
    }

    @Test
    public void testWikiTokens() throws Exception {
        List<CharBuffer> buffers = new ArrayList<>();
        CharBuffer buffer = CharBuffer.allocate(1);
        try (Reader wikiReader = new InputStreamReader(JsonTest.class.getResourceAsStream("/wiki.json"), UTF_8)) {
            while (wikiReader.read(buffer) > 0) {
                buffer.flip();
                buffers.add(buffer);
                buffer = CharBuffer.allocate(1);
            }
        }

        Observable<JsonToken> tokens = Observable.from(buffers)
                .compose(new JsonTokenTransformer());

        verifyWikiTokens(tokens);
    }

    private void verifyWikiTokens(Observable<JsonToken> tokens) {
        @SuppressWarnings("unchecked")
        Observer<JsonToken> observer = mock(Observer.class);
        tokens.subscribe(observer);

        JsonToken firstName = new JsonToken(JsonToken.Id.KEY, CharBuffer.wrap("firstName"));
        JsonToken john = new JsonToken(JsonToken.Id.VALUE_STRING, CharBuffer.wrap("John"));
        JsonToken lastName = new JsonToken(JsonToken.Id.KEY, CharBuffer.wrap("lastName"));
        JsonToken smith = new JsonToken(JsonToken.Id.VALUE_STRING, CharBuffer.wrap("Smith"));
        JsonToken age = new JsonToken(JsonToken.Id.KEY, CharBuffer.wrap("age"));
        JsonToken twoFive = new JsonToken(JsonToken.Id.VALUE_NUMBER, CharBuffer.wrap("25"));

        JsonToken address = new JsonToken(JsonToken.Id.KEY, CharBuffer.wrap("address"));
        JsonToken streetAddress = new JsonToken(JsonToken.Id.KEY, CharBuffer.wrap("streetAddress"));
        JsonToken street = new JsonToken(JsonToken.Id.VALUE_STRING, CharBuffer.wrap("21 2nd Street"));
        JsonToken city = new JsonToken(JsonToken.Id.KEY, CharBuffer.wrap("city"));
        JsonToken newYork = new JsonToken(JsonToken.Id.VALUE_STRING, CharBuffer.wrap("New York"));
        JsonToken state = new JsonToken(JsonToken.Id.KEY, CharBuffer.wrap("state"));
        JsonToken ny = new JsonToken(JsonToken.Id.VALUE_STRING, CharBuffer.wrap("NY"));
        JsonToken postalCode = new JsonToken(JsonToken.Id.KEY, CharBuffer.wrap("postalCode"));
        JsonToken code = new JsonToken(JsonToken.Id.VALUE_STRING, CharBuffer.wrap("10021"));

        JsonToken phoneNumber = new JsonToken(JsonToken.Id.KEY, CharBuffer.wrap("phoneNumber"));
        JsonToken type = new JsonToken(JsonToken.Id.KEY, CharBuffer.wrap("type"));
        JsonToken home = new JsonToken(JsonToken.Id.VALUE_STRING, CharBuffer.wrap("home"));
        JsonToken number = new JsonToken(JsonToken.Id.KEY, CharBuffer.wrap("number"));
        JsonToken fax = new JsonToken(JsonToken.Id.VALUE_STRING, CharBuffer.wrap("fax"));
        JsonToken homeNumber = new JsonToken(JsonToken.Id.VALUE_STRING, CharBuffer.wrap("212 555-1234"));
        JsonToken faxNumber = new JsonToken(JsonToken.Id.VALUE_STRING, CharBuffer.wrap("646 555-4567"));

        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).onNext(JsonToken.START_OBJECT);  // {

        inOrder.verify(observer).onNext(firstName);               //    "firstName" :
        inOrder.verify(observer).onNext(john);                    //                   "John"
        inOrder.verify(observer).onNext(lastName);                //    "lasName"   :
        inOrder.verify(observer).onNext(smith);                   //                   "Smith"
        inOrder.verify(observer).onNext(age);                     //    "age"       :
        inOrder.verify(observer).onNext(twoFive);                 //                   25
        inOrder.verify(observer).onNext(address);                 //    "address"   :

        inOrder.verify(observer).onNext(JsonToken.START_OBJECT);  //    {
        inOrder.verify(observer).onNext(streetAddress);           //        "streetAddress" :
        inOrder.verify(observer).onNext(street);                  //                           "21 2nd Street"
        inOrder.verify(observer).onNext(city);                    //        "city"          :
        inOrder.verify(observer).onNext(newYork);                 //                           "New York"
        inOrder.verify(observer).onNext(state);                   //        "state"         :
        inOrder.verify(observer).onNext(ny);                      //                           "NY"
        inOrder.verify(observer).onNext(postalCode);              //        "postalCode"    :
        inOrder.verify(observer).onNext(code);                    //                           "10021"
        inOrder.verify(observer).onNext(JsonToken.END_OBJECT);    //    }

        inOrder.verify(observer).onNext(phoneNumber);             //    "phoneNumber" :
        inOrder.verify(observer).onNext(JsonToken.START_ARRAY);   //    [
        inOrder.verify(observer).onNext(JsonToken.START_OBJECT);  //        {
        inOrder.verify(observer).onNext(type);                    //             "type"   :
        inOrder.verify(observer).onNext(home);                    //                         "home"
        inOrder.verify(observer).onNext(number);                  //             "number" :
        inOrder.verify(observer).onNext(homeNumber);              //                         "212 555-1234"
        inOrder.verify(observer).onNext(JsonToken.END_OBJECT);    //        }
        inOrder.verify(observer).onNext(JsonToken.START_OBJECT);  //        {
        inOrder.verify(observer).onNext(type);                    //             "type"   :
        inOrder.verify(observer).onNext(fax);                     //                         "fax"
        inOrder.verify(observer).onNext(number);                  //             "number" :
        inOrder.verify(observer).onNext(faxNumber);               //                         "646 555-4567"
        inOrder.verify(observer).onNext(JsonToken.END_OBJECT);    //        }
        inOrder.verify(observer).onNext(JsonToken.END_ARRAY);     //    ]

        inOrder.verify(observer).onNext(JsonToken.END_OBJECT);    // }

        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onCompleted();
    }

    @Test
    public void emptyObjectTokens() throws Exception {
        CharBuffer[] buffers = new CharBuffer[2];
        buffers[0] = CharBuffer.wrap("{");
        buffers[1] = CharBuffer.wrap("}");

        Observable<JsonToken> tokens = Observable.from(buffers)
                .compose(new JsonTokenTransformer());

        verifyEmptyObjectTokens(tokens);
    }

    @Test
    public void emptyObjectTokens1() throws Exception {
        CharBuffer[] buffers = new CharBuffer[1];
        buffers[0] = CharBuffer.wrap("{}");

        Observable<JsonToken> tokens = Observable.from(buffers)
                .compose(new JsonTokenTransformer());

        verifyEmptyObjectTokens(tokens);
    }

    @Test
    public void emptyObjectTokens2() throws Exception {
        CharBuffer[] buffers = new CharBuffer[1];
        buffers[0] = CharBuffer.wrap(" { } ");

        Observable<JsonToken> tokens = Observable.from(buffers)
                .compose(new JsonTokenTransformer());

        verifyEmptyObjectTokens(tokens);
    }

    private void verifyEmptyObjectTokens(Observable<JsonToken> tokens) {
        @SuppressWarnings("unchecked")
        Observer<JsonToken> observer = mock(Observer.class);
        tokens.subscribe(observer);
        verify(observer, times(1)).onNext(JsonToken.START_OBJECT);
        verify(observer, times(1)).onNext(JsonToken.END_OBJECT);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onCompleted();
    }

    @Test
    public void emptyArrayTokens() throws Exception {
        CharBuffer[] buffers = new CharBuffer[2];
        buffers[0] = CharBuffer.wrap("[");
        buffers[1] = CharBuffer.wrap("]");

        Observable<JsonToken> tokens = Observable.from(buffers)
                .compose(new JsonTokenTransformer());

        verifyEmptyArrayTokens(tokens);
    }

    @Test
    public void emptyArrayTokens1() throws Exception {
        CharBuffer[] buffers = new CharBuffer[1];
        buffers[0] = CharBuffer.wrap("[]");

        Observable<JsonToken> tokens = Observable.from(buffers)
                .compose(new JsonTokenTransformer());

        verifyEmptyArrayTokens(tokens);
    }

    @Test
    public void emptyArrayTokens2() throws Exception {
        CharBuffer[] buffers = new CharBuffer[1];
        buffers[0] = CharBuffer.wrap(" [ ] ");

        Observable<JsonToken> tokens = Observable.from(buffers)
                .compose(new JsonTokenTransformer());

        verifyEmptyArrayTokens(tokens);
    }

    private void verifyEmptyArrayTokens(Observable<JsonToken> tokens) {
        @SuppressWarnings("unchecked")
        Observer<JsonToken> observer = mock(Observer.class);
        tokens.subscribe(observer);
        verify(observer, times(1)).onNext(JsonToken.START_ARRAY);
        verify(observer, times(1)).onNext(JsonToken.END_ARRAY);
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onCompleted();
    }

}
