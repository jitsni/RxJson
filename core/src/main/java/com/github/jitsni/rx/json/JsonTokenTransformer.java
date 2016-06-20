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
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;

import java.nio.CharBuffer;

/**
 *
 * @author Jitendra Kotamraju
 */
public class JsonTokenTransformer implements Observable.Transformer<CharBuffer, JsonToken> {

    @Override
    public Observable<JsonToken> call(Observable<CharBuffer> source) {

        Observable<JsonToken> tokenObservable = Observable.create(new Observable.OnSubscribe<JsonToken>() {
            @Override
            public void call(Subscriber<? super JsonToken> subscriber) {
                final JsonTokenizer tokenizer = new JsonTokenizer(subscriber);

                source.subscribe(
                        new Subscriber<CharBuffer>() {
                            @Override
                            public void onCompleted() {
                                subscriber.onCompleted();
                            }

                            @Override
                            public void onError(Throwable throwable) {

                            }

                            @Override
                            public void onNext(CharBuffer buffer) {
                                tokenizer.parse(buffer);
                            }
                        }
                );
            }
        });

        return tokenObservable;
    }
}
