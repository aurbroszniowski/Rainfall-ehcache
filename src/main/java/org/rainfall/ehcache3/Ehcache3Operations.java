/*
 * Copyright 2014 Aurélien Broszniowski
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

package org.rainfall.ehcache3;

import org.rainfall.ehcache3.operation.GetOperation;
import org.rainfall.ehcache3.operation.PutOperation;
import org.rainfall.ehcache3.operation.RemoveOperation;

/**
 * Contains the helper methods to instantiate the Ehcache {@link org.rainfall.Operation} objects.
 *
 * @author Aurelien Broszniowski
 */
public class Ehcache3Operations {

  public static PutOperation put() {
    return new PutOperation();
  }

  public static GetOperation get() {
    return new GetOperation();
  }

  public static RemoveOperation remove() {
    return new RemoveOperation();
  }

}
