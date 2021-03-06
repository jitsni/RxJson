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

import java.nio.CharBuffer;
import java.util.Collections;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author Jitendra Kotamraju
 */
public final class JsonNumber implements JsonValue {

    private final CharBuffer buffer;

    JsonNumber(CharBuffer buffer) {
        this.buffer = buffer;
    }

    JsonNumber(String str) {
        this.buffer = CharBuffer.wrap(str);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.NUMBER;
    }

    @Override
    public Observable<JsonValue> observable() {
        return Observable.just(this);
    }

    @Override
    public Stream<JsonValue> stream() {
        return Collections.<JsonValue>singleton(this).stream();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JsonNumber) {
            return buffer.equals(((JsonNumber) obj).buffer);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return buffer.hashCode();
    }

    @Override
    public String toString() {
        return buffer.toString();
    }
}
