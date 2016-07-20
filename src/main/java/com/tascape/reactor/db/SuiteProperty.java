/*
 * Copyright 2015 - 2016 Nebula Bay.
 *
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
 */
package com.tascape.reactor.db;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author linsong wang
 */
@MappedSuperclass
@Table(name = "suite_property")
@XmlRootElement
public class SuiteProperty implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String TABLE_NAME = "suite_property";

    public static final String SUITE_PROPERTY_ID = "SUITE_PROPERTY_ID";

    public static final String SUITE_RESULT_ID = "SUITE_RESULT_ID";

    public static final String PROPERTY_NAME = "PROPERTY_NAME";

    public static final String PROPERTY_VALUE = "PROPERTY_VALUE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "SUITE_PROPERTY_ID")
    private Integer suitePropertyId;

    @Basic(optional = false)
    @Column(name = "SUITE_RESULT_ID")
    private String suiteResultId;

    @Column(name = "PROPERTY_NAME")
    private String propertyName;

    @Column(name = "PROPERTY_VALUE")
    private String propertyValue;

    @JoinColumn(name = "SUITE_RESULT_ID", referencedColumnName = "SUITE_RESULT_ID")
    @ManyToOne
    private SuiteResult suiteResult;

    public SuiteProperty() {
    }

    public SuiteProperty(Integer suitePropertyId) {
        this.suitePropertyId = suitePropertyId;
    }

    public SuiteProperty(Integer suitePropertyId, String suiteResultId) {
        this.suitePropertyId = suitePropertyId;
        this.suiteResultId = suiteResultId;
    }

    public Integer getSuitePropertyId() {
        return suitePropertyId;
    }

    public void setSuitePropertyId(Integer suitePropertyId) {
        this.suitePropertyId = suitePropertyId;
    }

    public String getSuiteResultId() {
        return suiteResultId;
    }

    public void setSuiteResultId(String suiteResultId) {
        this.suiteResultId = suiteResultId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public SuiteResult getSuiteResult() {
        return suiteResult;
    }

    public void setSuiteResult(SuiteResult suiteResult) {
        this.suiteResult = suiteResult;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (suitePropertyId != null ? suitePropertyId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SuiteProperty)) {
            return false;
        }
        SuiteProperty other = (SuiteProperty) object;
        if ((this.suitePropertyId == null && other.suitePropertyId != null) || (this.suitePropertyId != null
            && !this.suitePropertyId.equals(other.suitePropertyId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.tascape.qa.th.db.SuiteProperty[ suitePropertyId=" + suitePropertyId + " ]";
    }
}
