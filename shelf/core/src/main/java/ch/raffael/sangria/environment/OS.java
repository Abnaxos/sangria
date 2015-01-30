/*
 * Copyright 2012 Piratenpartei Schweiz
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
package ch.raffael.sangria.environment;


import com.google.inject.ProvidedBy;
import com.google.inject.Singleton;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
@ProvidedBy(OS.Provider.class)
@Singleton
public enum OS {

    
    WINDOWS(false),
    MAC(true),
    LINUX(true),
    UNIX(true);

    private static final OS current;
    static {
        String os = System.getProperty("os.name").toLowerCase();
        if ( os.startsWith("windows") ) {
            current = WINDOWS;
        }
        else if ( os.startsWith("mac os x") ) {
            current = MAC;
        }
        else if ( os.startsWith("linux") ) {
            current = LINUX;
        }
        else {
            // for now, we're simply assuming UNIX
            current = UNIX;
        }
    }

    private boolean unix;

    private OS(boolean unix) {
        this.unix = unix;
    }

    public static OS current() {
        return current;
    }

    public boolean unix() {
        return unix;
    }

    public static class Provider implements com.google.inject.Provider<OS> {
        @Override
        public OS get() {
            return OS.current();
        }
    }

}
