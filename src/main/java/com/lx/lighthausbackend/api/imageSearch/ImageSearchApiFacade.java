package com.lx.lighthausbackend.api.imageSearch;

import com.lx.lighthausbackend.api.imageSearch.model.ImageSearchResult;
import com.lx.lighthausbackend.api.imageSearch.sub.GetImageFirstUrlApi;
import com.lx.lighthausbackend.api.imageSearch.sub.GetImageListApi;
import com.lx.lighthausbackend.api.imageSearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索图片
     *
     * @param imageUrl
     * @return
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://lighthaus-1360289802.cos.ap-nanjing.myqcloud.com/public/1958755116771938306/2025-08-24_SuLxjfDQFACSx6P6.png";
        List<ImageSearchResult> resultList = searchImage(imageUrl);
        System.out.println("结果列表" + resultList);
    }
}
