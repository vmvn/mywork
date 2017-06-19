import { ELEMENT_URLS } from '../constants/ActionTypes';

import { WeaTools } from 'ecCom'; 

//获取门户信息api
const reqJobsInfoDatas = (params = {}) => {
    return WeaTools.callApi(ELEMENT_URLS.JOBSINFO_URL, 'POST', params);
}
module.exports = {
    reqJobsInfoDatas
};
