package io.github.appuhafeez.tiktoktoe.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.github.appuhafeez.tiktoktoe.model.AddHistoryRequest;

@FeignClient(fallback = HistoryMaintainerServiceProxyFallback.class,name = "${history.miantainer.base.url}")
public interface HistoryMaintainerServiceProxy {
	
	@RequestMapping(method = RequestMethod.POST, path = "/history/add")
	boolean addHistory(@RequestBody AddHistoryRequest addHistoryRequest);
	
}
