package org.turing.pangu.engine;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.turing.pangu.iptrunk.BaiduLocation;
import org.turing.pangu.model.App;
import org.turing.pangu.model.PhoneBrand;
import org.turing.pangu.phone.ChangeDeviceInfo;
import org.turing.pangu.phone.GenerateData;
import org.turing.pangu.phone.NetworkSubType;
import org.turing.pangu.phone.NetworkType;
import org.turing.pangu.service.BaseService;
import org.turing.pangu.service.PhoneBrandService;
import org.turing.pangu.service.PhoneBrandServiceImpl;
import org.turing.pangu.utils.RandomUtils;

public class PhoneBrandEngine implements EngineListen{
	private static final Logger logger = Logger.getLogger(PhoneBrandEngine.class);
	private static PhoneBrandEngine mInstance = new PhoneBrandEngine();
	private List<PhoneBrand> phoneBrandList = new ArrayList<PhoneBrand>();
	private PhoneBrandService phoneBrandService = null;
	public static final int CHINA_MOBILE = 0; // 移动
	public static final int CHINA_UNICOM = 1; // 联通
	public static final int CHINA_TELECOM = 2;// 电信
	
	
	public static PhoneBrandEngine getInstance()
	{
		if(null == mInstance)
			mInstance = new PhoneBrandEngine();
		return mInstance;
	}
	private int getOperator(){
		int random = RandomUtils.getRandom(0, 100);
		if(random > 0 && random < 60 ){
			return CHINA_MOBILE;
		}else if(random > 60 && random < 85){
			return CHINA_UNICOM;
		}else{
			return CHINA_TELECOM;
		}
	}
	public ChangeDeviceInfo getNewDeviceInfo(BaiduLocation location,App app){
		ChangeDeviceInfo info = new ChangeDeviceInfo();
		// 6:3:1
		//1.先产生品牌随机数
		int operator = getOperator();
		String operStr = "移动";
		PhoneBrand brand = null;
		//2.确定支持运营商品牌型号机型
		for(int flag = 0;flag == 0;){
			int random = RandomUtils.getRandom(0, phoneBrandList.size());
			brand = phoneBrandList.get(random);
			// 选择支持分辨率的型号
			if(true == ResolutionEngine.getInstance().isSupportResolution(brand,app.getPlatformId())){
				break;
			}
		}
		switch(operator){
		case CHINA_MOBILE:
			if(brand.getChinaMobile() == 1){
				operStr = "移动";
			}
			break;
		case CHINA_UNICOM:
			if(brand.getChinaUnicom() == 1){
				operStr = "联通";
			}
			break;
		case CHINA_TELECOM:
			if(brand.getChinaTelecom() == 1){
				operStr = "电信";
			}
			break;
		}
		String number = PhoneNumberEngine.getInstance().getPhoneNumber(location.getContent().getAddress(), operStr);
		if(null == number)
			return null;
		info.setSdk(GenerateData.getInstance().getSdk());
		info.setImsi(GenerateData.getInstance().generateImsi(operator));
		info.setImei(GenerateData.getInstance().generateImei(brand.getImeiHead()));
		info.setPhone(number);
		
		info.setBrand(brand.getBrand()); // 品牌
		info.setManufacture(brand.getBrand()); // 制造商
		
		info.setModel(brand.getModel());   //
		info.setProduct(brand.getModel()); //
		info.setHeight(brand.getHeight().toString());// 高
		info.setWidth(brand.getWidth().toString()); // 宽
		info.setSsid(WifiMngEngine.getInstance().getWifiName()); // wifi 名称
		//latitude 维度 -> y
		info.setLongitude(Double.parseDouble(location.getContent().getPoint().getX()));
		info.setLatitude(Double.parseDouble(location.getContent().getPoint().getY()));
		
		info.setAndroidId(GenerateData.getInstance().generateAndroidId());
		info.setAndroidSerial(GenerateData.getInstance().generateAndroidSerial());
		info.setAndroidVersion(GenerateData.getInstance().getAndroidVersion(Integer.parseInt(info.getSdk())));
		
		info.setBlueTooth(GenerateData.getInstance().generateBluetooth());
		info.setBssid(info.getBlueTooth());
		info.setCarrier(GenerateData.getInstance().generateCarrier(operator));
		info.setCarrierCode(GenerateData.getInstance().generateCarrierCode(operator));
		info.setCpuABI(GenerateData.getInstance().generateCpu());
		info.setDisplay(GenerateData.getInstance().generateDisplay(info.getSdk(), brand.getBrand()));
		
		info.setBoard("unknown");
		info.setSimSerial(GenerateData.getInstance().generateSimSerial(operator));
		info.setSimStatus("5");
		info.setBootloader(GenerateData.getInstance().generateBootloader(info.getSdk(), brand.getModel()));
		
		// 产生网络类型 80% wifi
		int random = RandomUtils.getRandom(0, 10);
		if(random < 7){
			info.setNetworkType(NetworkType.TYPE_WIFI);
			info.setNetworkTypeName("WIFI");
		}
		else{
			info.setNetworkType(NetworkType.TYPE_MOBILE);
			info.setNetworkTypeName("MOBILE");
		}
		// 3G 
		if(random % 3 == 0){
			info.setNetworkSubType(NetworkSubType.NETWORK_TYPE_HSPA);
			switch(operator){
				case CHINA_MOBILE:
					info.setNetworkSubTypeName("TD-SCDMA");
					break;
				case CHINA_UNICOM:
					info.setNetworkSubTypeName("WCDMA");
					break;
				case CHINA_TELECOM:
					info.setNetworkSubTypeName("CDMA2000");
					break;
			}
		}else{ // 4G
			info.setNetworkSubType(NetworkSubType.NETWORK_TYPE_LTE);
			switch(operator){
			case CHINA_MOBILE:
				info.setNetworkSubTypeName("TD-LTE");
				break;
			case CHINA_UNICOM:
				info.setNetworkSubTypeName("FDD-LTE");
				break;
			case CHINA_TELECOM:
				info.setNetworkSubTypeName("FDD-LTE");
				break;
			}
		}
		return info;
	}
	@Override
	public void setService(List<BaseService> serviceList) {
		// TODO Auto-generated method stub
		for(BaseService service :serviceList){
			if(service instanceof PhoneBrandServiceImpl ){
				this.phoneBrandService = (PhoneBrandService)service;
			}
		}
	}
	@Override
	public void init() {
		// TODO Auto-generated method stub
		phoneBrandList.clear();
		// 1. 先查出所有的品牌型号
		if(null != phoneBrandService){
			phoneBrandList = phoneBrandService.selectAll();
		}
		//2. 按权重添加品牌
		for(int index = 1; index < 5;index ++){
			for(PhoneBrand brand:phoneBrandList){
				if(brand.getWeight() > index){
					phoneBrandList.add(brand); // -- 权重越大，添加越多
				}
			}
		}
	}
	@Override
	public void open() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void upDate() {
		// TODO Auto-generated method stub
		
	}
}
