/*
 * Copyright (c) 2008, Gennady & Michael Kushnir
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 * 	•	Redistributions of source code must retain the above copyright notice, this
 * 		list of conditions and the following disclaimer.
 * 	•	Redistributions in binary form must reproduce the above copyright notice,
 * 		this list of conditions and the following disclaimer in the documentation
 * 		and/or other materials provided with the distribution.
 * 	•	Neither the name of the RUJEL nor the names of its contributors may be used
 * 		to endorse or promote products derived from this software without specific 
 * 		prior written permission.
 * 		
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

function analyse(container) {
	var ins = container.getElementsByTagName('textarea');
	if(ins)
		analyseIns(ins);
	ins = container.getElementsByTagName('input');
	if(ins)
		analyseIns(ins);
	ins = container.getElementsByTagName('select');
	if(ins) {
	for(var i = 0; i < ins.length; i++) {
		cl = ins[i].className;
		if (/onloadOnchange/i.test(cl)) {
			ins[i].onchange();
		}
	}}
}

function analyseIns (ins) {
	for(var i = 0; i < ins.length; i++) {
		cl = ins[i].className;
		if (/titled/i.test(cl)) {
			titulize(ins[i]);
			ins[i].onblur = new Function("titulize(this);");
			if(ins[i].form.onsubmit == null)
				ins[i].form.onsubmit = presubmit;
		} else if (/date/i.test(cl) || /decimal/i.test(cl)) {
			ins[i].onkeypress = new Function("event","return isNumberInput(event,true);");
		}  else if (/numeric/i.test(cl)) {
			ins[i].onkeypress = new Function("event","return isNumberInput(event,false);");
		}
		if (/onloadOnchange/i.test(cl)) {
			ins[i].onchange();
		}
		if(ins[i].type == "text" && (ins[i].maxLength > 255 || ins[i].maxLength < 0)) {
			ins[i].maxLength = 255;
		}
		if (/required/i.test(cl) && ins[i].form.onsubmit == null)
			ins[i].form.onsubmit = presubmit;
		if(ins[i].disabled && 
				ins[i].type != "submit" && ins[i].type != "button" && ins[i].type != "reset")
			ins[i].style.backgroundColor='#cccccc';
	}
}

function titulize(field) {
	if(field.value && field.value != field.title) {
		field.style.color = "#000000";
		field.onfocus = null;
	} else {
		field.value = field.title;
		field.style.color = "#cccccc";
		field.onfocus = new Function("this.value = '';this.style.color = '#000000';");
	}
}

function presubmit(form) {
	var fail = null;
	for(var i=0;i<form.elements.length;i++) {
		var elt = form.elements[i];
		if(elt && /titled/i.test(elt.className) && elt.value == elt.title)
			elt.value = ''; 
		if(elt && /required/i.test(elt.className)) {
			if(elt.value && elt.value.length > 0) {
				elt.style.border = '';
			} else {
				elt.style.border = '2pt dotted #ff0000';
				if(fail == null)
					fail = elt;
				if(/titled/i.test(elt.className))
					titulize(elt);
			}
		}
	}
	if(fail)
		fail.focus();
	return fail == null;
}

function isNumberInput(event,dot) {
	if ((event.charCode == null || event.charCode == 0) 
			&& event.keyCode >= 37 && event.keyCode <= 40)
		return true;
	var key = eventKey(event);
	if(key == null || key < 32 || key == 127)  // spec symbols
		return true;
	if (key >= 48 && key <= 57) // digits
		return true;
	if(dot && key == 46) // dot
		return true;
	if(dot && ((key >= 42 && key <= 47) || key == 1073 || key == 1102)) {
		if(event.srcElement)
			insertAtCursor(event.srcElement, '.');
		else if(event.target)
			insertAtCursor(event.target, '.');
	}
	if((event.ctrlKey || event.metaKey) && (key == 122 || key == 120 || key == 99 || key == 118))
		return true;
	return false;
}

function eventKey(event) {
	var key;	
	if(window.event) {
		key = window.event.keyCode;
		if(key == 0)
			key = window.event.charCode
	} else if (event) {
		key = event.keyCode;
		if(key == 0)
			key = event.charCode;
	} else {
		return 0;
	}
	return key;
}

function insertAtCursor(myField, myValue) {
  if (document.selection) {
  //IE support
    myField.focus();
    sel = document.selection.createRange();
    sel.text = myValue;
  } else if (myField.selectionStart || myField.selectionStart == '0') {
  //MOZILLA/NETSCAPE support
    var startPos = myField.selectionStart;
    var endPos = myField.selectionEnd;
    myField.value = myField.value.substring(0, startPos)
                  + myValue
                  + myField.value.substring(endPos, myField.value.length);
    myField.selectionEnd = startPos + myValue.length;
  } else {
    myField.value += myValue;
  }
}
