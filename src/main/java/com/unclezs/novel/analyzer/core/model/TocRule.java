package com.unclezs.novel.analyzer.core.model;

import com.unclezs.novel.analyzer.core.rule.CommonRule;
import com.unclezs.novel.analyzer.core.rule.RuleConstant;
import com.unclezs.novel.analyzer.model.Verifiable;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 目录规则
 * <pre>
 *     # 最小规则
 *     "toc": {
 *         "list": "匹配章节列表",
 *         "name": "章节名称",
 *         "url":  "章节的链接"
 *     }
 *     "toc":{
 *         "list": "匹配章节列表",
 *         "name": "章节名称",
 *         "url":  "章节的链接",
 *         "next": "下一页规则",
 *         "enableNext": ""是否允许下一页,
 *         "blackUrls": "黑名单链接",
 *         "filter": "允许章节过滤",
 *         "reserve": "章节逆序",
 *         "sort": "章节重排序",
 *     }
 * </pre>
 *
 * @author blog.unclezs.com
 * @date 2021/02/10 11:10
 */
@Data
public class TocRule implements Verifiable, Serializable {
    private static final long serialVersionUID = -6824834231883722483L;
    /**
     * 章节列表
     */
    private CommonRule list;
    /**
     * 章节名称
     */
    private CommonRule name;
    /**
     * 章节链接
     */
    private CommonRule url;
    /**
     * 章节下一页规则（存在则会匹配下一页）
     */
    private CommonRule next = CommonRule.create("xpath", RuleConstant.NEXT_PAGE_RULE);
    /**
     * 章节翻页
     */
    private boolean enableNext = true;
    /**
     * 章节黑名单链接（其中的链接不会加入到章节列表）
     */
    private Set<String> blackUrls;
    /**
     * 章节过滤
     */
    private boolean filter = true;
    /**
     * 章节逆序
     */
    private boolean reserve;
    /**
     * 章节乱序重排
     */
    private boolean sort;

    /**
     * 是否允许章节翻页 规则必须存在
     *
     * @return /
     */
    public boolean isAllowNextPage() {
        return enableNext && CommonRule.isEffective(next);
    }

    /**
     * 目录规则是否有效
     *
     * @param rule 目录规则
     * @return true 有效
     */
    public static boolean isEffective(TocRule rule) {
        return rule != null && rule.isEffective();
    }

    /**
     * 章节规则是否有效
     *
     * @return /
     */
    @Override
    public boolean isEffective() {
        return CommonRule.isEffective(list) && CommonRule.hasRule(name) && CommonRule.hasRule(url);
    }
}