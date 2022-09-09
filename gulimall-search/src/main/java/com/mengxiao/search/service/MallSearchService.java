package com.mengxiao.search.service;

import com.mengxiao.search.vo.SearchParam;
import com.mengxiao.search.vo.SearchResult;

public interface MallSearchService {
    /**
     * 检索的所有参数
     *
     * @param param
     * @return
     */
    SearchResult search(SearchParam param);
}
