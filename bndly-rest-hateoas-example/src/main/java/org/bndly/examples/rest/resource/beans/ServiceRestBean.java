package org.bndly.examples.rest.resource.beans;

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

import org.bndly.rest.atomlink.api.annotation.BeanID;
import org.bndly.rest.common.beans.RestBean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "service")
@XmlAccessorType(XmlAccessType.NONE)
public class ServiceRestBean extends ServiceReferenceRestBean {

    @XmlElement
    private String name;

    @XmlElements ({
            @XmlElement( name = "bundle", type = BundleRestBean.class),
            @XmlElement( name = "bundleRef", type = BundleReferenceRestBean.class)
    })
    private BundleReferenceRestBean bundle;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BundleReferenceRestBean getBundle() {
        return bundle;
    }

    public void setBundle(BundleReferenceRestBean bundle) {
        this.bundle = bundle;
    }
}
