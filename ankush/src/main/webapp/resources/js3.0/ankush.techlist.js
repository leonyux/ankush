/*******************************************************************************
*  ===========================================================
*  Ankush : Big Data Cluster Management Solution
*  ===========================================================
*  
*  (C) Copyright 2014, by Impetus Technologies
*  
*  This is free software; you can redistribute it and/or modify it under
*  the terms of the GNU Lesser General Public License (LGPL v3) as
*  published by the Free Software Foundation;
*  
*  This software is distributed in the hope that it will be useful, but
*  WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
*  See the GNU Lesser General Public License for more details.
*  
*  You should have received a copy of the GNU Lesser General Public License 
*  along with this software; if not, write to the Free Software Foundation, 
* Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*******************************************************************************/
function capitalize(str) {
    strVal = '';
    str = str.split(' ');
    for (var chr = 0; chr < str.length; chr++) {
        strVal += str[chr].substring(0, 1).toUpperCase() + str[chr].substring(1, str[chr].length) + ' ';
    }
    return strVal;
}
com.impetus.ankush.selectTechnology = {
		createCluster : function(cluster,templateName,clusterTechnology){
			var url="";
			if(undefined == templateName || templateName==""){
				url = baseUrl + '/'+cluster+'/clusterCreate/C-D/'+clusterTechnology;
			}else {
				url = baseUrl + '/'+cluster+'/clusterCreation/C-D/'+clusterTechnology+'/'+templateName;
			}
			/*if(undefined == templateName){
				url = baseUrl + '/'+cluster+'/clusterCreation/C-D/';
			}else{
				url = baseUrl + '/'+cluster+'/clusterCreation/C-D/'+templateName;
			}*/
			 $('#technologyDialogBox').modal('hide');
				 title = capitalize(cluster.split('-').join(" "))+"Creation";
				 tooltipTittle='Cluster Overview';
				   $(location).attr('href',(url));
			     /*$('#content-panel').sectionSlider('addChildPanel', {
	 		    	current : 'login-panel',
			    
			        method : 'get',
			        title : title,
			        tooltipTitle :tooltipTittle ,
	    			params : {
	    				templateName:templateName,
	    			},
	                callback : function(){
	                	
	                }		 
	    			
	  		});*/
		}	
};