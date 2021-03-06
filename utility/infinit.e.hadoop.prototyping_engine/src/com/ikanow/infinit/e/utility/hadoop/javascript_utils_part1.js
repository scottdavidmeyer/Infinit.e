
//////////////////////////////////////////////////////

importPackage(org.bson.types);
importPackage(java.util);
importPackage(java.text);

//////////////////////////////////////////////////////

// INPUT/INTERMEDIATE UTILITIES

var _emit_list = [];

function emit(_key, _val)
{
	_val._id = null;
	_emit_list.push({key:_key, val:_val});
}

//////////////////////////////////////////////////////

// OUTPUT UTILITIES

function s1(el) {
	if (el == null) {}
	else if (el instanceof Array) {
		s2(el, 1);
	}
	else if (typeof el == 'object') {
		outList.add(s3(el));
	}
	else {
		outList.add(el.toString());
	}
}
function s2(el, master_list) {
	var list = (1 == master_list)?outList:listFactory.clone();
	for (var i = 0; i < el.length; ++i) {
		var subel = el[i];
		if (subel == null) {}
		else if (subel instanceof Array) {
			list.add(s2(subel, 0));
		}
		else if (typeof subel == 'object') {
			list.add(s3(subel));
		}
		else {
			list.add(subel.toString());
		}
	}
	return list;
}
function s3(el) {
	el.constructor.toString();
	
	// MongoDB specific:
	if (null != el['$oid']) {
		return new org.bson.types.ObjectId(el['$oid']);
	}
	else if (null != el['$date']) {
		var ISOFORMAT = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		return ISOFORMAT.parse(el['$date'].replace('Z','-0000').toString());
		
	}
	
	// Others:
	var currObj = objFactory.clone();
	for (var prop in el) {
		var subel = el[prop];
		if (subel == null) {}
		else if (subel instanceof Array) {
			currObj.put(prop, s2(subel, 0));
		}
		else if (typeof subel == 'object') {
			currObj.put(prop, s3(subel));
		}
		else {
			currObj.put(prop, subel.toString());
		}
	}
	return currObj;
}

