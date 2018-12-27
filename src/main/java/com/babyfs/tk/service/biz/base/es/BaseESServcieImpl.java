package com.babyfs.tk.service.biz.base.es;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.babyfs.tk.commons.model.ServiceResponse;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.script.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.alibaba.fastjson.serializer.SerializerFeature.DisableCircularReferenceDetect;
import static org.elasticsearch.common.xcontent.XContentFactory.*;
/**
 *
 */
public class BaseESServcieImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseESServcieImpl.class);

    protected static final int TIMEOUT = 5;
    /**
     * ES索引的名称
     */
    protected final String indexName;
    /**
     * ES文档的名称
     */
    protected final String docName;

    @Inject
    protected Client client;

    /**
     * @param indexName ES 索引的名称,非空
     * @param docName   ES文档的名称,非空
     */
    public BaseESServcieImpl(String indexName, String docName) {
        this.indexName = Preconditions.checkNotNull(StringUtils.trimToNull(indexName));
        this.docName = Preconditions.checkNotNull(StringUtils.trimToNull(docName));
    }

    /**
     * 重建索引文档
     *
     * @param docId 文档的id,not null
     * @param doc   文档的内容,not null
     * @return 索引是否成功
     */
    public ServiceResponse<Void> indexDoc(String docId, String doc) {
        Preconditions.checkNotNull(docId);
        Preconditions.checkNotNull(doc);

        IndexRequestBuilder builder = client.prepareIndex(this.indexName, this.docName, docId);
        try {
            builder.setSource(buildEsObject(doc));
        } catch (IOException e) {
            LOGGER.error("Index " + this.indexName + "." + this.docName + "/" + docId, e);
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, e.getMessage());
        }

        try {
            builder.execute().actionGet(TIMEOUT, TimeUnit.SECONDS);
            return ServiceResponse.succResponse();
        } catch (Exception e) {
            LOGGER.error("Index " + this.indexName + "." + this.docName + "/" + docId, e);
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, e.getMessage());
        }
    }

    /**
     * (部分)更新文档
     *
     * @param docId       文档的id,not null
     * @param doc         文档更新的内容,not null
     * @param docAsUpsert dos as upsert
     * @return 更新是否成功
     */
    public ServiceResponse<Void> updateDoc(String docId, String doc, boolean docAsUpsert) {
        Preconditions.checkNotNull(docId);
        Preconditions.checkNotNull(doc);

        UpdateRequestBuilder requestBuilder = client.prepareUpdate(this.indexName, this.docName, docId);
        try{
            requestBuilder.setDoc(buildEsObject(doc));
        }catch (IOException e){
            LOGGER.error("Document update " + this.indexName + "." + this.docName + "/" + docId, e);
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, e.getMessage());
        }
        requestBuilder.setDocAsUpsert(docAsUpsert);

        try {
            requestBuilder.execute().actionGet(TIMEOUT, TimeUnit.SECONDS);
            return ServiceResponse.succResponse();
        } catch (Exception e) {
            LOGGER.error("Document update " + this.indexName + "." + this.docName + "/" + docId, e);
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, e.getMessage());
        }
    }

    /**
     * 脚本更新文档
     *
     * @param docId          文档的id,not null
     * @param script         script,not null
     * @param scriptedUpsert scriptedUpset
     * @return 更新是否成功
     */
    public ServiceResponse<Void> updateScript(String docId, Script script, boolean scriptedUpsert) {
        Preconditions.checkNotNull(docId);
        Preconditions.checkNotNull(script);

        UpdateRequestBuilder requestBuilder = client.prepareUpdate(this.indexName, this.docName, docId);
        requestBuilder.setScript(script);
        requestBuilder.setScriptedUpsert(scriptedUpsert);

        try {
            requestBuilder.execute().actionGet(TIMEOUT, TimeUnit.SECONDS);
            return ServiceResponse.succResponse();
        } catch (Exception e) {
            LOGGER.error("Script update " + this.indexName + "." + this.docName + "/" + docId, e);
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, e.getMessage());
        }
    }

    /**
     * 删除文档
     *
     * @param docId 文档id,not null
     * @return 删除是否成功
     */
    public ServiceResponse<Void> delete(String docId) {
        Preconditions.checkNotNull(docId);

        DeleteRequestBuilder builder = client.prepareDelete(this.indexName, this.docName, docId);

        try {
            builder.execute().actionGet(TIMEOUT, TimeUnit.SECONDS);
            return ServiceResponse.succResponse();
        } catch (Exception e) {
            LOGGER.error("Delete " + this.indexName + "." + this.docName + "/" + docId, e);
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, e.getMessage());
        }
    }

    /**
     * 删除文档
     *
     * @param id
     * @return
     */
    public ServiceResponse<Void> delete(long id) {
        return this.delete(String.valueOf(id));
    }

    /**
     * @param o
     * @param filters
     * @return
     */
    protected String createDoc(Object o, SerializeFilter[] filters) {
        return JSONObject.toJSONString(o, SerializeConfig.getGlobalInstance(), filters, DisableCircularReferenceDetect);

    }

    /**
     * 批量新增索引文档
     *
     * @param map 文档的id,not null  文档的内容,not null
     * @return 索引是否成功
     */
    public ServiceResponse<Void> bulkDoc(Map<String, String> map) {

        Preconditions.checkNotNull(map);


        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();

        for(String docId : map.keySet()){
            IndexRequestBuilder builder = client.prepareIndex(this.indexName, this.docName, docId);
// es is upgraded
// builder.setSource(map.get(docId));
            try {
                builder.setSource(buildEsObject(map.get(docId)));
            } catch (IOException e) {
                LOGGER.error("errr build es object", e);
                continue;
            }
            bulkRequestBuilder.add(builder);
        }
        try {
            bulkRequestBuilder.execute().actionGet(TIMEOUT, TimeUnit.SECONDS);
            return ServiceResponse.succResponse();
        } catch (Exception e) {
            LOGGER.error("Index " + this.indexName + "." + this.docName, e);
            return ServiceResponse.createFailResponse(ServiceResponse.FAIL_KEY, e.getMessage());
        }
    }

    private XContentBuilder buildEsObject(String jsonString) throws IOException {
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        Set<Map.Entry<String, Object>> entries = jsonObject.entrySet();

        XContentBuilder xContentBuilder = jsonBuilder().startObject();
        for (Map.Entry<String, Object> ent :
                entries) {
            xContentBuilder.field(ent.getKey(), ent.getValue());
        }
        return xContentBuilder.endObject();
    }

}
