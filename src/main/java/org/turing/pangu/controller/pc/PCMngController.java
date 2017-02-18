package org.turing.pangu.controller.pc;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.turing.pangu.controller.common.BaseController;
import org.turing.pangu.controller.common.PGResponse;
import org.turing.pangu.controller.pc.request.CurdVpnReq;
import org.turing.pangu.controller.pc.request.GetAppListReq;
import org.turing.pangu.controller.pc.request.GetDynamicVpnListReq;
import org.turing.pangu.controller.pc.request.GetTaskListReq;
import org.turing.pangu.controller.pc.request.GetRemainIpListReq;
import org.turing.pangu.controller.pc.request.GetRemainVpnListReq;
import org.turing.pangu.controller.pc.request.LoginReq;
import org.turing.pangu.controller.pc.request.SaveBlackIpListReq;
import org.turing.pangu.controller.pc.request.VpnLoginReq;
import org.turing.pangu.controller.pc.request.VpnOperUpdateReq;
import org.turing.pangu.controller.pc.request.VpnSwitchFinishReq;
import org.turing.pangu.controller.pc.response.VpnLoginRsp;
import org.turing.pangu.controller.pc.response.VpnOperUpdateRsp;
import org.turing.pangu.controller.phone.request.GetBlackIpListReq;
import org.turing.pangu.engine.TaskConfigureEngine;
import org.turing.pangu.engine.TaskEngine;
import org.turing.pangu.model.App;
import org.turing.pangu.model.DynamicVpn;
import org.turing.pangu.model.Platform;
import org.turing.pangu.model.RemainData;
import org.turing.pangu.model.RemainVpn;
import org.turing.pangu.model.Task;
import org.turing.pangu.model.User;
import org.turing.pangu.service.AppService;
import org.turing.pangu.service.DeviceService;
import org.turing.pangu.service.DynamicVpnService;
import org.turing.pangu.service.PlatformService;
import org.turing.pangu.service.RemainDataService;
import org.turing.pangu.service.RemainVpnService;
import org.turing.pangu.service.TaskService;
import org.turing.pangu.service.UserService;
import org.turing.pangu.utils.Const;
import org.turing.pangu.utils.DateUtils;

import com.alibaba.fastjson.JSON;

/**
 * 
 * @author turing
 * @des 处理手机端过来的所有关于用户信息相关请求（如：注册，登录，修改密码）
 *
 */
@Controller("pcMngController")
@RequestMapping("/pc")
public class PCMngController extends BaseController {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(PCMngController.class);

	@Resource(name="platformServiceImpl")
	private PlatformService platformService;
	
	@Resource(name="appServiceImpl")
	private AppService appService;
	
	@Resource(name="remainVpnServiceImpl")
	private RemainVpnService remainVpnService;
	
	@Resource(name="dynamicVpnServiceImpl")
	private DynamicVpnService dynamicVpnService;
	
	@Resource(name="remainDataServiceImpl")
	private RemainDataService remainDataService;
	
	@Resource(name="deviceServiceImpl")
	private DeviceService deviceService;
	
	@Resource(name="userServiceImpl")
	private UserService userService;
	
	@Resource(name="taskServiceImpl")
	private TaskService taskService;
	
	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public @ResponseBody PGResponse<String> index() {
		logger.info("index---" + new Date());
		PGResponse<String> rsp = new PGResponse<String>();
		TaskEngine.getInstance().setService(platformService, appService, deviceService, taskService);
		TaskEngine.getInstance().init();
		TaskConfigureEngine.getInstance().init();
		rsp.setAllData(Const.common_ok, "common_ok", null);
		logger.info("index---" + JSON.toJSONString(rsp).toString());
		return rsp;
	}
	
	@RequestMapping(value = "/createTodayTask", method = RequestMethod.GET)
	public @ResponseBody PGResponse<String> createTodayTask() {
		logger.info("createTodayTask---" + new Date());
		PGResponse<String> rsp = new PGResponse<String>();
		TaskEngine.getInstance().createTodayTask();
		rsp.setAllData(Const.common_ok, "common_ok", null);
		logger.info("createTodayTask---" + JSON.toJSONString(rsp).toString());
		return rsp;
	}
	@RequestMapping(value = "/remoteIp", method = RequestMethod.GET)
	public @ResponseBody PGResponse<String> remoteIp(HttpServletRequest request) {
		logger.info("remoteIp---" + new Date());
		PGResponse<String> rsp = new PGResponse<String>();
		String remoteIp = TaskEngine.getInstance().getRemoteIp(request);
		rsp.setAllData(Const.common_ok, "common_ok", remoteIp);
		return rsp;
	}
	@RequestMapping(value = "/proxyIp", method = RequestMethod.GET)
	public @ResponseBody PGResponse<String> proxyIp(HttpServletRequest request) {
		logger.info("proxyIp---" + new Date());
		PGResponse<String> rsp = new PGResponse<String>();
		String realIp = TaskEngine.getInstance().getRealIp(request);
		rsp.setAllData(Const.common_ok, "common_ok", realIp);
		return rsp;
	}
	
	// vpn登录请求
	@RequestMapping(value = "/vpnLogin", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody PGResponse<VpnLoginRsp> vpnLogin(@RequestBody VpnLoginReq req,HttpServletRequest request) {
		logger.info("vpnLogin---" + req.getDeviceId() + new Date());
		PGResponse<VpnLoginRsp> rsp = new PGResponse<VpnLoginRsp>();
		String remoteIp = TaskEngine.getInstance().getRemoteIp(request);
		String realIp = TaskEngine.getInstance().getRealIp(request);
		String token = TaskEngine.getInstance().addVpnTask(req,remoteIp,realIp);
		VpnLoginRsp dataRsp = new VpnLoginRsp();
		dataRsp.setToken(token);
		dataRsp.setRemoteIp(remoteIp);
		dataRsp.setRealIp(realIp);
		dataRsp.setLoopTime(10); // 暂定10S
		rsp.setAllData(Const.common_ok, "common_ok", dataRsp);
		logger.info("vpnLogin---" + JSON.toJSONString(rsp).toString());
		return rsp;
	}
	// vpn操作请求
	@RequestMapping(value = "/vpnOperUpdate", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody PGResponse<VpnOperUpdateRsp> vpnOperUpdate(@RequestBody VpnOperUpdateReq req,HttpServletRequest request) {
		logger.info("vpnOperUpdate---" + req.getToken() + new Date());
		PGResponse<VpnOperUpdateRsp> rsp = new PGResponse<VpnOperUpdateRsp>();
		String remoteIp = TaskEngine.getInstance().getRemoteIp(request);
		String realIp = TaskEngine.getInstance().getRealIp(request);
		boolean ret = TaskEngine.getInstance().vpnIsNeedSwitch(req.getToken());
		VpnOperUpdateRsp dataRsp = new VpnOperUpdateRsp();
		dataRsp.setRemoteIp(remoteIp);
		dataRsp.setRealIp(realIp);
		if(true == ret){
			dataRsp.setIsSwitchVpn(1);
		}else{
			dataRsp.setIsSwitchVpn(0);
		}
		dataRsp.setLoopTime(10); // 暂定10S
		rsp.setAllData(Const.common_ok, "common_ok", dataRsp);
		logger.info("vpnOperUpdate---" + JSON.toJSONString(rsp).toString());
		return rsp;
	}
	// vpn切换完成
	@RequestMapping(value = "/vpnSwitchFinish", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody PGResponse<String> vpnSwitchFinish(@RequestBody VpnSwitchFinishReq req,HttpServletRequest request) {
		logger.info("vpnSwitchFinish---" + req.getToken() + new Date());
		PGResponse<String> rsp = new PGResponse<String>();
		String remoteIp = TaskEngine.getInstance().getRemoteIp(request);
		String realIp = TaskEngine.getInstance().getRealIp(request);
		TaskEngine.getInstance().switchVpnFinish(req.getToken(), remoteIp, realIp);
		rsp.setAllData(Const.common_ok, "common_ok", "");
		logger.info("vpnSwitchFinish---" + JSON.toJSONString(rsp).toString());
		return rsp;
	}
	
	
	@RequestMapping(value = "/login", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody PGResponse<String> login(@RequestBody LoginReq req) {
		logger.info("login---" + req.getName() + "--" + new Date());
		PGResponse<String> rsp = new PGResponse<String>();
		User user = new User();
		user.setName(req.getName());
		user.setPassword(req.getPassword());
		List<User> list = userService.selectList(user);
		if(null == list || list.size() == 0 )
		{
			rsp.setAllData(Const.common_error, "common_error", "");
		}else
		{
			rsp.setAllData(Const.common_ok, "common_ok", "");
		}
		logger.info("login---" + JSON.toJSONString(rsp).toString());
		return rsp;
	}
	/**
	 * 获取VPN列表接口
	 * 
	 * @return
	 */
	@RequestMapping(value = "/getDynamicVpnList", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody PGResponse<List<DynamicVpn>> getDynamicVpnList(@RequestBody GetDynamicVpnListReq req) {
		logger.info("getDynamicVpnList---" + new Date());
		PGResponse<List<DynamicVpn>> rsp = new PGResponse<List<DynamicVpn>>();
		List<DynamicVpn> list = dynamicVpnService.selectAll();
		rsp.setAllData(Const.common_ok, "common_ok", list);
		logger.info("getDynamicVpnList---" + JSON.toJSONString(rsp).toString());
		return rsp;
	}
	
	/**
	 * 获取固定IPVPN列表接口
	 * 
	 * @return
	 */
	@RequestMapping(value = "/getTodayRemainIpList", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody PGResponse<String> getTodayRemainIpList(@RequestBody GetRemainIpListReq req) {
		logger.info("getTodayRemainIpList---" + new Date());
		PGResponse<String> rsp = new PGResponse<String>();
		String iplist = deviceService.getRemainIpList();
		rsp.setAllData(Const.common_ok, "common_ok", iplist);
		logger.info("getTodayRemainIpList---" + JSON.toJSONString(rsp).toString());
		return rsp;
	}
	/**
	 * 获取黑名单IP
	 * 
	 * @return
	 */
	@RequestMapping(value = "/getBlackIpList", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody PGResponse<String> getBlackIpList(@RequestBody GetBlackIpListReq req) {
		logger.info("getBlackIpList---" + new Date());
		PGResponse<String> rsp = new PGResponse<String>();
		Platform platform = platformService.select(req.getPlatformId());
		rsp.setAllData(Const.common_ok, "common_ok", platform.getBlackIp());
		logger.info("getBlackIpList---" + JSON.toJSONString(rsp).toString());
		return rsp;
	}
	/**
	 * 保存黑名单IP
	 * 
	 * @return
	 */
	@RequestMapping(value = "/saveBlackIpList", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody PGResponse<String> saveBlackIpList(@RequestBody SaveBlackIpListReq req) {
		logger.info("saveBlackIpList---" + new Date());
		PGResponse<String> rsp = new PGResponse<String>();
		Platform platform  = platformService.select(req.getPlatformId());
		if(platform != null && !req.getIpList().equals("")){
			platform.setBlackIp(req.getIpList());
			platform.setUpdateDate(new Date());
			platformService.update(platform);
		}
		rsp.setAllData(Const.common_ok, "common_ok", "");
		logger.info("saveBlackIpList---" + JSON.toJSONString(rsp).toString());
		return rsp;
	}
	
	/**
	 * 获取固定IPVPN列表接口
	 * 
	 * @return
	 */
	@RequestMapping(value = "/getFixedVpnList", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody PGResponse<List<RemainVpn>> getFixedVpnList(@RequestBody GetRemainVpnListReq req) {
		logger.info("getRemainVpnList---" + new Date());
		PGResponse<List<RemainVpn>> rsp = new PGResponse<List<RemainVpn>>();
		List<RemainVpn> list = remainVpnService.selectAll();
		rsp.setAllData(Const.common_ok, "common_ok", list);
		logger.info("getRemainVpnList---" + JSON.toJSONString(rsp).toString());
		return rsp;
	}
	/**
	 * 获取app列表信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/getAppList", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody PGResponse<List<App>> getAppList(@RequestBody GetAppListReq req) {
		logger.info("getAppList---" + req.getUserId() + "--" + new Date());
		PGResponse<List<App>> rsp = new PGResponse<List<App>>();
		App model = new App();
		model.setIsCanRun(1);
		List<App> list = appService.selectCanRunApps(model);
		rsp.setAllData(Const.common_ok, "common_ok", list);
		logger.info("getAppList---" + JSON.toJSONString(rsp).toString());
		return rsp;
	}
	
	/**
	 * 获取游戏全部运营数据接口
	 * 
	 * @return
	 */
	@RequestMapping(value = "/getTaskDataList", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody PGResponse<List<Task>> getTaskDataList(@RequestBody GetTaskListReq req) {
		logger.info("getTaskDataList---" + req.getAppId() + "--" + new Date());
		PGResponse<List<Task>> rsp = new PGResponse<List<Task>>();
		Task data = new Task();
		data.setAppId(req.getAppId());
		List<Task> list = taskService.selectList(data);
		rsp.setAllData(Const.common_ok, "common_ok", list);
		logger.info("getTaskDataList---" + JSON.toJSONString(rsp).toString());
		return rsp;
	}
	
	/**
	 * 获取留存文件地址
	 * 
	 * @return
	 */
	@Deprecated
	@RequestMapping(value = "/getRemainFilePath", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody PGResponse<List<RemainData>> getRemainFilePath(@RequestBody GetTaskListReq req) {
		logger.info("getRemainFilePath---" + req.getAppId() + "--" + new Date());
		PGResponse<List<RemainData>> rsp = new PGResponse<List<RemainData>>();
		RemainData data = new RemainData();
		//Date todayMorning = DateUtils.getTimesMorning();
		//Date todayNight = DateUtils.getTimesNight();
		data.setAppId(req.getAppId());
		data.setCreateDate(req.getStartDate());
		data.setUpdateDate(req.getEndDate());
		List<RemainData> list = remainDataService.getRemainData(data);
		rsp.setAllData(Const.common_ok, "common_ok", list);
		logger.info("getRemainFilePath---" + JSON.toJSONString(rsp).toString());
		return rsp;
	}
	
	/**
	 * 获取今日实时数据情况
	 * 
	 * @return
	 */
	@RequestMapping(value = "/getRemain", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody PGResponse<List<RemainData>> getRemain(@RequestBody GetTaskListReq req) {
		logger.info("getRemain---" + req.getAppId() + "--" + new Date());
		PGResponse<List<RemainData>> rsp = new PGResponse<List<RemainData>>();
		RemainData data = new RemainData();
		Date todayMorning = DateUtils.getTimesMorning();
		Date todayNight = DateUtils.getTimesNight();
		data.setAppId(req.getAppId());
		data.setCreateDate(todayMorning);
		data.setUpdateDate(todayNight);
		List<RemainData> list = remainDataService.getRemainData(data);
		rsp.setAllData(Const.common_ok, "common_ok", list);
		logger.info("getRemain---" + JSON.toJSONString(rsp).toString());
		return rsp;
	}
	
	/**
	 * 获取VPN列表接口
	 * 
	 * @return
	 */
	@RequestMapping(value = "/curdVpn", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody PGResponse<String> curdVpn(@RequestBody CurdVpnReq req) {
		logger.info("curdVpn---" + req.getName() + "--" + new Date());
		PGResponse<String> rsp = new PGResponse<String>();
		RemainVpn model = new RemainVpn();
		if( req.getType() == 1 ) //增
		{
			model.setIpList(req.getIpList());
			model.setName(req.getName());
			model.setCreateDate(new Date());
			model.setUpdateDate(new Date());
			remainVpnService.insert(model);
		}else if( req.getType() == 2 ){ //改
			model.setIpList(req.getIpList());
			model.setName(req.getName());
			model.setId(req.getId());
			model.setCreateDate(new Date());
			model.setUpdateDate(new Date());
			remainVpnService.update(model);
		}else{
			rsp.setAllData(Const.common_error, "common_error", "");
			return rsp;
		}
		rsp.setAllData(Const.common_ok, "common_ok", "");
		return rsp;
	}
}
