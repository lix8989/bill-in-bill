package com.lex.wechatbill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("llm_settings")
public class LlmSettingsEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String configKey;
    private String configValue;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
}
