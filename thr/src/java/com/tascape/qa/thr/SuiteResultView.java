package com.tascape.qa.thr;

import com.tascape.qa.th.db.DbHandler.Suite_Result;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
@Named
@RequestScoped
public class SuiteResultView implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(SuiteResultView.class);

    private static final long serialVersionUID = 1L;

    @Inject
    private MySqlBaseBean db;

    private String srid;

    private boolean toggleInvisible = false;

    private Map<String, Object> suiteResult;

    private List<Map<String, Object>> testsResult;

    @PostConstruct
    public void init() {
        this.getParameters();

        try {
            this.suiteResult = this.db.getSuiteResult(this.srid);
            boolean invisible = this.suiteResult.get(Suite_Result.INVISIBLE_ENTRY.name()).equals(1);
            if (this.toggleInvisible) {
                this.setInvisible(!invisible);
                return;
            }
            this.testsResult = this.db.getTestsResult(this.srid);
            this.testsResult.stream().forEach(row -> {
                row.put("_trid", StringUtils.right(row.get("TEST_RESULT_ID") + "", 12));
                row.put("_class", StringUtils.substringAfterLast(row.get("TEST_CLASS") + "", "."));
            });
        } catch (NamingException | SQLException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Map<String, Object> getSuiteResult() {
        return suiteResult;
    }

    public List<Map<String, Object>> getTestsResult() {
        return testsResult;
    }

    public String getSrid() {
        return srid;
    }

    private void setInvisible(boolean invisible) throws NamingException, SQLException, IOException {
        this.db.setSuiteResultInvisible(this.srid, invisible);
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.redirect(context.getRequestContextPath() + context.getRequestServletPath() + "?srid=" + srid);
    }

    private void getParameters() {
        Map<String, String> map = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String v = map.get("srid");
        LOG.debug("srid = {}", v);
        if (v != null) {
            this.srid = v;
        }
        v = map.get("ti");
        LOG.debug("toggle invisible = {}", v);
        if (v != null) {
            this.toggleInvisible = Boolean.parseBoolean(v);
        }
    }
}
