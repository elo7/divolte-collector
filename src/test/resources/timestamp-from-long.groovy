/*
 * Copyright 2019 GoDataDriven B.V.
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

mapping {
    map firstInSession() onto 'sessionStart'
    map remoteHost() onto 'remoteHost'

    // Check that we can map a long onto a timestamp field.
    map 1558101158679 onto 'ts'
    map 1558101158679123 onto 'tsMicros'
}
