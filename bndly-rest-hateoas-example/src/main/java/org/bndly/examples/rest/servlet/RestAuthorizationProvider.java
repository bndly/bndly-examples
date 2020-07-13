package org.bndly.examples.rest.servlet;

/*-
 * #%L
 * Bndly examples REST HATEOAS
 * %%
 * Copyright (C) 2020 Cybercon GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.bndly.rest.api.Context;
import org.bndly.rest.api.SecurityHandler;
import org.osgi.service.component.annotations.Component;

@Component(
        service = { SecurityHandler.AuthorizationProvider.class },
        immediate = true
)
public class RestAuthorizationProvider implements SecurityHandler.AuthorizationProvider {

    @Override
    public boolean isAnonymousAllowed(Context context) {
        // at this place you can handle access to services which so not need authorization
        // this would be a login service as an example
        return false;
    }

    @Override
    public boolean isAuthorized(Context context, String user, String password) {
        // this could be the place where you limit access based on user roles etc.
        return true;
    }
}
