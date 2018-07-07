package com.tongtong.purchaser.utils;

public class UrlUtil {
	public static final String BASE_URL = "https://app.ascwl.com:8443/";
	public static final String IMG_SERVER_URL = "https://app.ascwl.com:8443";
	public static final String VEDIO_SERVER_URL = "https://app.ascwl.com:8443";
	public static final String GET_CONFIG = BASE_URL+"PhonePurchaserMyInfo/getConfig";
	public static final String GET_MARQUEEN = BASE_URL+"PhonePurchaserMyInfo/getMarqueenString";
	public static final String GET_MONEY = BASE_URL+"PhonePurchaserMyInfo/getmoney";
	public static final String GET_FARMER_INFO = BASE_URL+"PhonePurchaserMyInfo/getFarmerInfo";
	//登录模块
	public static final String REGISTER_URL = BASE_URL+"PhonePurchaserLogin/register";
	public static final String FIND_PASSWORD_URL = BASE_URL+"PhonePurchaserLogin/findPassword";
	public static final String LOGIN_URL = BASE_URL+"PhonePurchaserLogin/login";
	public static final String GET_ORDER_LIST = BASE_URL+"PhonePurchaserOrder/getpurchaserorderlist";
	public static final String GET_ORDER_NUM = BASE_URL+"PhonePurchaserOrder/getpurchaserordernum";
	public static final String GET_MSG_LIST = BASE_URL+"PhonePurchaserMyInfo/getMsgList";
	public static final String UPDATE_YIDU = BASE_URL+"PhonePurchaserMyInfo/yidu";
	public static final String DELETE_MSG = BASE_URL+"PhonePurchaserMyInfo/deleteMsg";
	public static final String GET_MSG_TYPE_LIST = BASE_URL+"PhonePurchaserMyInfo/getMsgTypeList";
	public static final String GET_NEW_MSG_COUNT = BASE_URL+"PhonePurchaserMyInfo/getNewMsgCount";
	//个人信息模块
	public static final String CHECK_VERSION = BASE_URL+"apkfiles/checkVersion";
	public static final String UPDATE_HEAD_ICON_URL = BASE_URL+"PhonePurchaserMyInfo/updateHeadIcon";
	public static final String UPDATE_GEO_THUMB_URL = BASE_URL+"PhonePurchaserMyInfo/uploadThumb";
	public static final String UPDATE_PASSWORD_URL = BASE_URL+"PhonePurchaserMyInfo/updatePassword";
	public static final String UPDATE_NAME_URL = BASE_URL+"PhonePurchaserMyInfo/updateName";
	public static final String FIND_REGION_URL = BASE_URL+"PhonePurchaserMyInfo/finRegion";
	public static final String UPDATE_ADDRESS_URL = BASE_URL+"PhonePurchaserMyInfo/updateAddress";
	public static final String SEARCH_COMPANY = BASE_URL+"PhonePurchaserMyInfo/searchCompany";
	public static final String ADD_COMPANY = BASE_URL+"PhonePurchaserMyInfo/addCompany";
	public static final String INIT_DATA = BASE_URL+"PhonePurchaserMyInfo/initData";
	public static final String CANCELL_ORDER = BASE_URL+"PhonePurchaserOrder/cancellorder";
	public static final String CHOICE_COMPANY = BASE_URL+"PhonePurchaserMyInfo/choiceCompany";
	public static final String GET_ORDER = BASE_URL+"PhonePurchaserOrder/getorderbypurchaser";
	public static final String GENARATE_SHOUGOU = BASE_URL+"PhonePurchaserOrder/genarateshougou";
	public static final String GET_SUGEESET_ADDRESS = BASE_URL+"PhoneFarmerRelease/getSuggestionAddress";
	//发布模块
	public static final String SEARCH_PRODUCE = BASE_URL+"PhonePurchaserRelease/searchProduce";
	public static final String RELEASE = BASE_URL+"PhonePurchaserRelease/release";
	public static final String UPDATE_RELEASE = BASE_URL+"PhonePurchaserRelease/updateRelease";
	public static final String DELETE_RELEASE = BASE_URL+"PhonePurchaserRelease/deleteRelease";
	public static final String ADDORDER = BASE_URL+"PhonePurchaserOrder/addorder";
	public static final String FIND_RELEASE = BASE_URL+"PhonePurchaserRelease/finRelease";
	public static final String UPDATE_LOCATION = BASE_URL+"PhonePurchaserRelease/updateLocation";
	public static final String QUREY_FARMER_RELEASE_MAIN = BASE_URL+"PhonePurchaserRelease/getrelease";
	public static final String QUREY_FARMER_RELEASE_MAIN_LIST = BASE_URL+"PhonePurchaserRelease/getreleaseList";
	public static final String QUREY_FARMER_RELEASE= BASE_URL+"PhonePurchaserRelease/qureyFarmerRelease";
	public static final String SELECT_COLLECTION= BASE_URL+"PhonePurchaserRelease/selectCollection";
	public static final String UPDATE_COLLECTION= BASE_URL+"PhonePurchaserRelease/updateCollection";
	public static final String SELECT_COLLECTION_RELEASE= BASE_URL+"PhonePurchaserRelease/selectCollectionRelease";
	public static final String SELECT_HISTORY_RELEASE= BASE_URL+"PhonePurchaserRelease/selectHistoryRelease";
	public static final String SELECT_SALE_RELEASE= BASE_URL+"PhonePurchaserRelease/selectSaleRelease";
	public static final String GET_RECMEND_DATA= BASE_URL+"PhonePurchaserMyInfo/getrecmendlist";
	public static final String SEARCH_RELEASE= BASE_URL+"PhonePurchaserRelease/searchRelease";
	public static final String GET_GUIGE= BASE_URL+"PhonePurchaserRelease/getGuige";
	public static final String GET_FARMER_RELEASE= BASE_URL+"PhonePurchaserRelease/getReleaseInfo";
	public static final String OPEN_LOGIN= BASE_URL+"PhonePurchaserLogin/openLogin";
	public static final String SEND_CODE= BASE_URL+"PhonePurchaserLogin/sendCode";
	public static final String CHECK_CODE= BASE_URL+"PhonePurchaserLogin/checkCode";
	public static final String CHECK_CODE_LOGIN= BASE_URL+"PhonePurchaserLogin/checkLoginCode";
	public static final String SAVE_INFO= BASE_URL+"PhonePurchaserLogin/saveInfo";
	public static final String SHARE_URL= "https://app.ascwl.com/invite/myinvite?uid=";
	public static final String XIEYI_URL= "https://app.ascwl.com/invite/xieyi";
	public static final String GUANYU_URL= "https://app.ascwl.com/invite/guanyu";
	public static final String GET_RP_COUNT = BASE_URL+"PhonePurchaserRelease/gethongbaocountbydistance";
	public static final String GET_RP_LIST = BASE_URL+"PhonePurchaserRelease/gethongbaobydistance";
	public static final String ROB_RP = BASE_URL+"PhoneHongbaoController/openrpbypurchaser";
	public static final String GET_NEARBY_COUNT = BASE_URL+"PhoneHongbaoController/getNearByCount";
	public static final String SEND_RP = BASE_URL+"PhoneHongbaoController/fahongbaobypurchaser";
	public static final String SEND_RP_WX_ORDER = BASE_URL+"pay/weixinpay";
	public static final String SEND_RP_BY_YUE = BASE_URL+"PhoneHongbaoController/fahongbaobyyuepurchaser";
	public static final String SEND_RP_ORDER = BASE_URL+"pay/alipay";
	public static final String GET_CARD_INFO = BASE_URL+"thirdApi/getCardInfo";
	public static final String ROB_RPS = BASE_URL+"PhoneHongbaoController/robrpbypurchaser";
	public static final String GET_HB_LIST = BASE_URL+"PhoneHongbaoController/gethblist";
	public static final String GET_RP_SEND_LIST = BASE_URL+"PhoneHongbaoController/getrpsendbypurchaser";
	public static final String GET_RP_RECORD_LIST = BASE_URL+"PhoneHongbaoController/getrprecordbypurchaser";
	public static final String GET_MINGXI_LIST = BASE_URL+"PhoneHongbaoController/getmingxibypurchaser";
	public static final String GET_INTEGERAL = BASE_URL+"PhonePurchaserLogin/getIntegerals";
    public static final String ADD_FAVOUR = BASE_URL+"favourates/addFavour";
    public static final String DELETE_FAVOUR = BASE_URL+"favourates/deleteFavour";
	public static final String GET_RELEASE_LIST = BASE_URL+"PhonePurchaserMyInfo/getReleaseList";
	public static final String GET_FAVOURITES_LIST = BASE_URL+"PhonePurchaserRelease/getFavourList";
}