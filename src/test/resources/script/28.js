page = utils.get(utils.absUrl('https://m.28ts.com/mp3/1651/1.html',result));url = utils.match(page, "regex:url\\d+? = '(http.+?)'##$1");suffix = utils.match(page, "regex:url\\d+?[+]*?'(.+?)'##$1");result = url+suffix;

