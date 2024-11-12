
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 指南留言
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/zhinanLiuyan")
public class ZhinanLiuyanController {
    private static final Logger logger = LoggerFactory.getLogger(ZhinanLiuyanController.class);

    private static final String TABLE_NAME = "zhinanLiuyan";

    @Autowired
    private ZhinanLiuyanService zhinanLiuyanService;


    @Autowired
    private TokenService tokenService;

    @Autowired
    private DictionaryService dictionaryService;//字典
    @Autowired
    private ForumService forumService;//论坛
    @Autowired
    private LiuyanService liuyanService;//留言板
    @Autowired
    private NewsService newsService;//社区公告
    @Autowired
    private YishengService yishengService;//医生
    @Autowired
    private YishengChatService yishengChatService;//用户咨询
    @Autowired
    private YishengCollectionService yishengCollectionService;//医生收藏
    @Autowired
    private YishengCommentbackService yishengCommentbackService;//医生评价
    @Autowired
    private YishengYuyueService yishengYuyueService;//挂号
    @Autowired
    private YonghuService yonghuService;//用户
    @Autowired
    private ZhinanService zhinanService;//防范指南
    @Autowired
    private ZhinanCollectionService zhinanCollectionService;//指南收藏
    @Autowired
    private UsersService usersService;//管理员


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        else if("医生".equals(role))
            params.put("yishengId",request.getSession().getAttribute("userId"));
        CommonUtil.checkMap(params);
        PageUtils page = zhinanLiuyanService.queryPage(params);

        //字典表数据转换
        List<ZhinanLiuyanView> list =(List<ZhinanLiuyanView>)page.getList();
        for(ZhinanLiuyanView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        ZhinanLiuyanEntity zhinanLiuyan = zhinanLiuyanService.selectById(id);
        if(zhinanLiuyan !=null){
            //entity转view
            ZhinanLiuyanView view = new ZhinanLiuyanView();
            BeanUtils.copyProperties( zhinanLiuyan , view );//把实体数据重构到view中
            //级联表 防范指南
            //级联表
            ZhinanEntity zhinan = zhinanService.selectById(zhinanLiuyan.getZhinanId());
            if(zhinan != null){
            BeanUtils.copyProperties( zhinan , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setZhinanId(zhinan.getId());
            }
            //级联表 用户
            //级联表
            YonghuEntity yonghu = yonghuService.selectById(zhinanLiuyan.getYonghuId());
            if(yonghu != null){
            BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setYonghuId(yonghu.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody ZhinanLiuyanEntity zhinanLiuyan, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,zhinanLiuyan:{}",this.getClass().getName(),zhinanLiuyan.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("用户".equals(role))
            zhinanLiuyan.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        zhinanLiuyan.setCreateTime(new Date());
        zhinanLiuyan.setInsertTime(new Date());
        zhinanLiuyanService.insert(zhinanLiuyan);

        return R.ok();
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody ZhinanLiuyanEntity zhinanLiuyan, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,zhinanLiuyan:{}",this.getClass().getName(),zhinanLiuyan.toString());
        ZhinanLiuyanEntity oldZhinanLiuyanEntity = zhinanLiuyanService.selectById(zhinanLiuyan.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("用户".equals(role))
//            zhinanLiuyan.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        zhinanLiuyan.setUpdateTime(new Date());

            zhinanLiuyanService.updateById(zhinanLiuyan);//根据id更新
            return R.ok();
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<ZhinanLiuyanEntity> oldZhinanLiuyanList =zhinanLiuyanService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        zhinanLiuyanService.deleteBatchIds(Arrays.asList(ids));

        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //.eq("time", new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
        try {
            List<ZhinanLiuyanEntity> zhinanLiuyanList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            ZhinanLiuyanEntity zhinanLiuyanEntity = new ZhinanLiuyanEntity();
//                            zhinanLiuyanEntity.setZhinanId(Integer.valueOf(data.get(0)));   //指南 要改的
//                            zhinanLiuyanEntity.setYonghuId(Integer.valueOf(data.get(0)));   //用户 要改的
//                            zhinanLiuyanEntity.setZhinanLiuyanText(data.get(0));                    //留言内容 要改的
//                            zhinanLiuyanEntity.setInsertTime(date);//时间
//                            zhinanLiuyanEntity.setReplyText(data.get(0));                    //回复内容 要改的
//                            zhinanLiuyanEntity.setUpdateTime(sdf.parse(data.get(0)));          //回复时间 要改的
//                            zhinanLiuyanEntity.setCreateTime(date);//时间
                            zhinanLiuyanList.add(zhinanLiuyanEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        zhinanLiuyanService.insertBatch(zhinanLiuyanList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }




    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        CommonUtil.checkMap(params);
        PageUtils page = zhinanLiuyanService.queryPage(params);

        //字典表数据转换
        List<ZhinanLiuyanView> list =(List<ZhinanLiuyanView>)page.getList();
        for(ZhinanLiuyanView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段

        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        ZhinanLiuyanEntity zhinanLiuyan = zhinanLiuyanService.selectById(id);
            if(zhinanLiuyan !=null){


                //entity转view
                ZhinanLiuyanView view = new ZhinanLiuyanView();
                BeanUtils.copyProperties( zhinanLiuyan , view );//把实体数据重构到view中

                //级联表
                    ZhinanEntity zhinan = zhinanService.selectById(zhinanLiuyan.getZhinanId());
                if(zhinan != null){
                    BeanUtils.copyProperties( zhinan , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setZhinanId(zhinan.getId());
                }
                //级联表
                    YonghuEntity yonghu = yonghuService.selectById(zhinanLiuyan.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody ZhinanLiuyanEntity zhinanLiuyan, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,zhinanLiuyan:{}",this.getClass().getName(),zhinanLiuyan.toString());
        zhinanLiuyan.setCreateTime(new Date());
        zhinanLiuyan.setInsertTime(new Date());
        zhinanLiuyanService.insert(zhinanLiuyan);

            return R.ok();
        }

}

