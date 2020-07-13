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

import org.bndly.rest.common.beans.ListRestBean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "services")
@XmlAccessorType(XmlAccessType.NONE)
public class ServiceListRestBean extends ListRestBean<ServiceReferenceRestBean> {

    @XmlElements({
        @XmlElement( name = "service", type = ServiceRestBean.class),
        @XmlElement( name = "serviceRef", type = ServiceReferenceRestBean.class)
    })
    private List<ServiceReferenceRestBean> items = new ArrayList<>();

    public ServiceListRestBean() {
    }

    @Override
    public void setItems(List<ServiceReferenceRestBean> items) {
        this.items = items;
    }

    @Override
    public List<ServiceReferenceRestBean> getItems() {
        return items;
    }
}
