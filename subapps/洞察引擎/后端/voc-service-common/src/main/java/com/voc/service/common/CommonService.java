//package com.voc.service.common;
//
//import cn.hutool.core.bean.BeanUtil;
//import com.voc.service.common.pagination.Page;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
///**
// * @version 1.0.0
// * @ClassName CommonService.java
// * @Description
// * @createTime 2022年09月13日 16:53
// * @Copyright futong
// */
//@Service
//public class CommonService {
//
//
//    /**
//     * 获取当前用户渠道标识集合
//     *
//     * @return
//     */
//
//
//    public IPage<?> converPage(com.baomidou.mybatisplus.core.metadata.IPage page) {
//        IPage rt = new Page();
//        BeanUtil.copyProperties(page, rt);
//
//        /*com.voc.service.common.pagination.Page myPage = new com.voc.service.common.pagination.Page();
//        myPage.setPages(page.getPages());
////        myPage.setCurrent(page.getCurrent());
//        myPage.setSize(page.getSize());
//        myPage.setRecords(page.getRecords());
//        myPage.setTotal(page.getTotal());
//
//        return myPage;*/
//
//        return rt;
//    }
//
//    public com.baomidou.mybatisplus.core.metadata.IPage<?> converPage(Page page) {
//
//       /* com.baomidou.mybatisplus.extension.plugins.pagination.Page myPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page();
////        myPage.setPages(page.getPages());
////        myPage.setCurrent(page.getCurrent());
////        myPage.setSize(page.getSize());
//        myPage.setRecords(page.getRecords());*/
////        myPage.setTotal(page.getTotal());
//        com.baomidou.mybatisplus.extension.plugins.pagination.Page rt = new com.baomidou.mybatisplus.extension.plugins.pagination.Page();
//        BeanUtil.copyProperties(page, rt);
//
//        return rt;
//    }
//
//}
