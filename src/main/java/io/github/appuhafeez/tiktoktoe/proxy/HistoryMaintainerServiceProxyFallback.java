package io.github.appuhafeez.tiktoktoe.proxy;

import org.springframework.stereotype.Component;

import io.github.appuhafeez.tiktoktoe.model.AddHistoryRequest;

@Component
public class HistoryMaintainerServiceProxyFallback implements HistoryMaintainerServiceProxy{

	@Override
	public boolean addHistory(AddHistoryRequest addHistoryRequest) {
		return false;
	}

}
