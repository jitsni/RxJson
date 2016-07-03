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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 *
 * @author Jitendra Kotamraju
 */
public final class JsonObject implements JsonValue {

    private final Map<String, Object> values = new LinkedHashMap<>();

    @Override
    public ValueType getValueType() {
        return ValueType.OBJECT;
    }

    void add(String key, Object object) {
        values.put(key, object);
    }

    @Override
    public Observable<JsonValue> observable() {
        return Observable.just(this);   // TODO
    }

    @Override
    public Stream<JsonValue> stream() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JsonObject) {
            return values.equals(((JsonObject) obj).values);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
