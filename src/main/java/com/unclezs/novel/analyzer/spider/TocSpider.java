package com.unclezs.novel.analyzer.spider;

import com.unclezs.novel.analyzer.common.page.AbstractPageable;
import com.unclezs.novel.analyzer.core.NovelMatcher;
import com.unclezs.novel.analyzer.core.comparator.ChapterComparator;
import com.unclezs.novel.analyzer.core.helper.AnalyzerHelper;
import com.unclezs.novel.analyzer.core.matcher.matchers.RegexMatcher;
import com.unclezs.novel.analyzer.core.model.AnalyzerRule;
import com.unclezs.novel.analyzer.core.model.TocRule;
import com.unclezs.novel.analyzer.model.Chapter;
import com.unclezs.novel.analyzer.request.RequestParams;
import com.unclezs.novel.analyzer.spider.helper.SpiderHelper;
import com.unclezs.novel.analyzer.util.CollectionUtils;
import com.unclezs.novel.analyzer.util.StringUtils;
import com.unclezs.novel.analyzer.util.uri.UrlUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 小说目录爬虫
 *
 * @author blog.unclezs.com
 * @date 2021/2/12 22:13
 */
@Slf4j
@NoArgsConstructor
public class TocSpider extends AbstractPageable<Chapter> {
    /**
     * 规则
     */
    @Getter
    private AnalyzerRule rule;
    /**
     * 请求目录参数
     */
    private RequestParams params;
    /**
     * 小说目录唯一ID
     */
    private String uniqueId = "none";
    private int order = 1;

    /**
     * 章节顺序比较器
     */
    public static final ChapterComparator CHAPTER_COMPARATOR = new ChapterComparator();

    public TocSpider(AnalyzerRule rule) {
        this.rule = rule;
        // 不忽略错误
        super.setIgnoreError(false);
    }

    /**
     * 获取小说目录
     *
     * @param url 目录URL
     * @throws IOException IO异常
     */
    public void toc(String url) throws IOException {
        toc(RequestParams.create(url));
    }

    /**
     * 获取小说目录
     *
     * @param params 请求参数
     * @throws IOException IO异常
     */
    public void toc(RequestParams params) throws IOException {
        this.order = 1;
        this.params = params.copy();
        super.firstLoad();
    }

    /**
     * 目录唯一ID
     *
     * @param item 数据项
     * @return 唯一标识
     */
    @Override
    protected String getUniqueId(Chapter item) {
        return item.getUrl();
    }

    /**
     * 加载一页目录
     *
     * @param page 下一页页码
     * @return true 还有更多
     * @throws IOException 网页请求异常
     */
    @Override
    protected boolean loadPage(int page) throws IOException {
        TocRule tocRule = getRule().getToc();
        // 获取网页内容
        String originalText = SpiderHelper.request(null, params);
        List<Chapter> chapters = NovelMatcher.toc(originalText, tocRule);
        // 预处理目录
        chapters = pretreatmentToc(chapters, params.getUrl(), tocRule, this.order);
        boolean hasMore = false;
        if (tocRule.isAllowNextPage()) {
            // 获取网页唯一ID 为 网页标题只留下了中文（不包含零到十）
            String uniqueId = RegexMatcher.me().titleWithoutNumber(originalText);
            String nextPageUrl = AnalyzerHelper.nextPage(originalText, tocRule.getNext(), params.getUrl());
            // 下一页存在 条件：下一页不为空 && 唯一ID相等或者是第一页 && 允许翻页
            hasMore = StringUtils.isNotBlank(nextPageUrl) && (Objects.equals(uniqueId, this.uniqueId) || page == 1);
            if (CollectionUtils.isNotEmpty(chapters)) {
                hasMore = addItems(chapters) && hasMore;
                log.trace("小说目录 第{}页 抓取完成，共{}章.", page, chapters.size());
            }
            if (hasMore) {
                this.uniqueId = uniqueId;
                this.params.setUrl(nextPageUrl);
            } else {
                // 已经抓取完成
                log.debug("小说目录:{} 抓取完成，共{}页.", params.getUrl(), page);
            }
        }
        return hasMore;
    }

    /**
     * 预处理目录
     *
     * @param toc     目录
     * @param baseUrl BaseURL
     * @param tocRule 规则
     * @param order   起始序号
     * @return 预处理后的目录
     */
    public static List<Chapter> pretreatmentToc(List<Chapter> toc, String baseUrl, TocRule tocRule, int order) {
        if (CollectionUtils.isNotEmpty(toc)) {
            // 去重，并且移除javascript的链接
            toc = toc.stream()
                .distinct()
                .filter(chapter -> !chapter.getUrl().startsWith("javascript"))
                .collect(Collectors.toList());
            if (tocRule != null) {
                // 移除黑名单列表
                if (CollectionUtils.isNotEmpty(tocRule.getBlackUrls())) {
                    List<Chapter> blackList = toc.stream()
                        .filter(chapter -> tocRule.getBlackUrls().contains(chapter.getUrl()))
                        .collect(Collectors.toList());
                    toc.removeAll(blackList);
                }
                // 章节过滤
                if (tocRule.isFilter()) {
                    toc = AnalyzerHelper.filterImpuritiesChapters(toc);
                }
            }
            // 自动拼接完整URL
            toc.stream()
                .filter(chapter -> !UrlUtils.isHttpUrl(chapter.getUrl()))
                .forEach(chapter -> chapter.setUrl(UrlUtils.completeUrl(baseUrl, chapter.getUrl())));
            if (tocRule != null) {
                // 乱序重排
                if (tocRule.isSort()) {
                    toc.sort(CHAPTER_COMPARATOR);
                }
            }
            // 编号
            for (Chapter chapter : toc) {
                chapter.setOrder(order++);
            }
        }
        return toc;
    }
}
