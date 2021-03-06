
     /*
   * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
   *
   * Copyright 2017 摩码创想, support@momathink.com
    *
   * This file is part of Jiaowu_v1.0.
   * Jiaowu_v1.0 is free software: you can redistribute it and/or modify
   * it under the terms of the GNU Lesser General Public License as published by
   * the Free Software Foundation, either version 3 of the License, or
   * (at your option) any later version.
   *
   * Jiaowu_v1.0 is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   * GNU Lesser General Public License for more details.
   *
   * You should have received a copy of the GNU Lesser General Public License
   * along with Jiaowu_v1.0.  If not, see <http://www.gnu.org/licenses/>.
   *
   * 这个文件是Jiaowu_v1.0的一部分。
   * 您可以单独使用或分发这个文件，但请不要移除这个头部声明信息.
    * Jiaowu_v1.0是一个自由软件，您可以自由分发、修改其中的源代码或者重新发布它，
   * 新的任何修改后的重新发布版必须同样在遵守LGPL3或更后续的版本协议下发布.
   * 关于LGPL协议的细则请参考COPYING文件，
   * 您可以在Jiaowu_v1.0的相关目录中获得LGPL协议的副本，
   * 如果没有找到，请连接到 http://www.gnu.org/licenses/ 查看。
   *
   * - Author:摩码创想
   * - Contact: support@momathink.com
   * - License: GNU Lesser General Public License (GPL)
   */

package com.momathink.teaching.subject.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.jfinal.kit.StrKit;
import com.jfinal.upload.UploadFile;
import com.momathink.common.base.BaseService;
import com.momathink.common.tools.ToolImport;
import com.momathink.teaching.subject.model.Subject;

public class SubjectService extends BaseService {
	private static Logger log = Logger.getLogger(SubjectService.class);
	public static final Subject dao = new Subject();
	public List<Subject> findAvailableSubject() {
		log.info("查询有效科目");
		return dao.findSubjectByState(0);
	}
	
	/**
	 * 保存导入
	 */
	public static  String importSubjects(UploadFile file,Integer createuserid) {
		// 获取文件
		String msg = "导入失败,请检查数据格式是否正确";
		try {
			//path = 文件目录 + 文件名称
			String fileName = file.getFileName();
			//判断后缀是否已xls
			if(fileName.toLowerCase().endsWith("xls")){
				// 处理导入数据
				Map<String, Object> flagList = ToolImport.dealDataByPath(file.getFile(), ImportSubject.tabMap, ImportSubject.mustTab);
				boolean flag = (boolean) flagList.get("flag");
				if (!flag) {
				} else {
					@SuppressWarnings("unchecked")
					List<Map<String, String>> list = (List<Map<String, String>>) flagList.get("list"); // 分析EXCEL数据
					
					Map<String, Object> saveMsg =  forAddXLSDB(list,createuserid);
					
					StringBuffer sb = new StringBuffer("您成功导入了 ").append(saveMsg.get("save")).append(" 条信息   <br>");
					sb.append("本次导入信息如下：<br>").append(flagList.get("errormsg")).append("<br>").append(saveMsg.get("saveMsg"));
					msg = sb.toString();
				}
			}else{
				msg = "上传文件只能为.xls类型";
			}
			ToolImport.removeTempFile(file);
			return msg;
		} catch (Exception e) {
			e.printStackTrace();
			ToolImport.removeTempFile(file);
			return msg;
		}
	}
	
	/**
	 * 把xls文件内容写入数据库
	 * @param tabDBName
	 * @param list
	 * @return
	 */
	public static  Map<String,Object> forAddXLSDB(List<Map<String, String>> list,Integer createuserid){
		Map<String, Object> saveMsg = new HashMap<String, Object>();
		StringBuffer msg = new StringBuffer();
		int save = 0;
		String key = null;
		String value = null;
		
		try {
			for (Map<String, String> map : list) { // 遍历取出的数据，并保存
				Subject subject = new Subject();//用户对象
				for (Map.Entry<String, String> entry : map.entrySet()) {
					key = entry.getKey();
					value = entry.getValue();
					if(StrKit.notBlank(value))
						value = value.trim();
					subject.set(key, value );
				}
				try{
					
					boolean saveflag = subject.save();//bookUser保存到数据库
					if(saveflag){
						save++;//保存几条数据
					}else{
						msg.append("第:"+ (save+1) +"  条信息存入失败");
						msg.append("<br>");
					}
				}catch(Exception ex){
					String exStr = ex.getLocalizedMessage();
					exStr = exStr.substring(exStr.indexOf(":"));
					msg.append("第:"+ (save+1) +"  条信息存入异常" + exStr);
					msg.append("<br>");
					ex.printStackTrace();
				}
			}
		} catch (Exception e) {
			msg.append("导入异常.");
			e.printStackTrace();
		}
		saveMsg.put("save", save);
		saveMsg.put("saveMsg", msg);
		return saveMsg;
	}

}
