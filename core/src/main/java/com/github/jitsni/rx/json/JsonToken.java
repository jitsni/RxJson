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

import java.nio.CharBuffer;
import java.util.Objects;

/**
 *
 * @author Jitendra Kotamraju
 */
public final class JsonToken {

    public enum Id {
        START_OBJECT,
        START_ARRAY,
        KEY,
        VALUE_STRING,
        VALUE_NUMBER,
        VALUE_TRUE,
        VALUE_FALSE,
        VALUE_NULL,
        END_ARRAY,
        END_OBJECT
    }

    public static final JsonToken START_OBJECT = new JsonToken(Id.START_OBJECT, null);
    public static final JsonToken START_ARRAY = new JsonToken(Id.START_ARRAY, null);
    public static final JsonToken VALUE_TRUE = new JsonToken(Id.VALUE_TRUE, null);
    public static final JsonToken VALUE_FALSE = new JsonToken(Id.VALUE_FALSE, null);
    public static final JsonToken VALUE_NULL = new JsonToken(Id.VALUE_NULL, null);
    public static final JsonToken END_ARRAY = new JsonToken(Id.END_ARRAY, null);
    public static final JsonToken END_OBJECT = new JsonToken(Id.END_OBJECT, null);

    private final Id id;
    private final CharBuffer buffer;

    JsonToken(Id id, CharBuffer buffer) {
        this.id = id;
        this.buffer = buffer;
    }

    public Id event() {
        return id;
    }

    public CharBuffer buffer() {
        return buffer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JsonToken) {
            JsonToken other = (JsonToken) obj;
            return Objects.equals(id, other.id) && Objects.equals(buffer, other.buffer);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, buffer);
    }

    @Override
    public String toString() {
        return id.toString();
    }

}
