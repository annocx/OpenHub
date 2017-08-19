/*
 *    Copyright 2017 ThirtyDegressRay
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.thirtydegreesray.openhub.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;

import com.thirtydegreesray.dataautoaccess.annotation.AutoAccess;
import com.thirtydegreesray.openhub.R;
import com.thirtydegreesray.openhub.inject.component.AppComponent;
import com.thirtydegreesray.openhub.inject.component.DaggerFragmentComponent;
import com.thirtydegreesray.openhub.inject.module.FragmentModule;
import com.thirtydegreesray.openhub.mvp.contract.IViewerContract;
import com.thirtydegreesray.openhub.mvp.presenter.ViewerPresenter;
import com.thirtydegreesray.openhub.ui.fragment.base.BaseFragment;
import com.thirtydegreesray.openhub.ui.widget.webview.PrettifyWebView;
import com.thirtydegreesray.openhub.util.BundleBuilder;
import com.thirtydegreesray.openhub.util.StringUtils;
import com.thirtydegreesray.openhub.util.ViewHelper;

import butterknife.BindView;

/**
 * Created by ThirtyDegreesRay on 2017/8/19 15:59:55
 */

public class ViewerFragment extends BaseFragment<ViewerPresenter>
        implements IViewerContract.View,
        PrettifyWebView.OnContentChangedListener,
        SwipeRefreshLayout.OnRefreshListener{

    @BindView(R.id.prettify_web_view) PrettifyWebView webView;
    @BindView(R.id.refresh_layout) SwipeRefreshLayout refreshLayout;

    @AutoAccess boolean wrap = false;

    @NonNull
    public static ViewerFragment create(Context context, String url, String htmlUrl) {
        ViewerFragment fragment = new ViewerFragment();
        fragment.setArguments(BundleBuilder.builder().put("url", url).put("htmlUrl", htmlUrl).build());
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_viewer;
    }

    @Override
    protected void setupFragmentComponent(AppComponent appComponent) {
        DaggerFragmentComponent.builder()
                .appComponent(appComponent)
                .fragmentModule(new FragmentModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void initFragment(Bundle savedInstanceState) {
        refreshLayout.setColorSchemeColors(ViewHelper.getRefreshLayoutColors(getContext()));
        refreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem menuItem = menu.findItem(R.id.action_wrap_lines);
        if(mPresenter.isCode() && !StringUtils.isBlank(mPresenter.getDownloadSource())){
            menuItem.setChecked(wrap);
            menuItem.setVisible(true);
        }else{
            menuItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_wrap_lines){
            item.setChecked(!item.isChecked());
            wrap = item.isChecked();
            loadCode(mPresenter.getDownloadSource());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void loadImageUrl(@NonNull String url) {
        webView.loadImage(url);
        webView.setOnContentChangedListener(this);
    }

    @Override
    public void loadMdText(@NonNull String text, @NonNull String baseUrl) {
        webView.setGithubContent(text, baseUrl);
        webView.setOnContentChangedListener(this);
    }

    @Override
    public void loadCode(@NonNull String text) {
        webView.setSource(text, wrap);
        webView.setOnContentChangedListener(this);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void showLoading() {
        super.showLoading();
        refreshLayout.setRefreshing(true);
    }

    @Override
    public void hideLoading() {
        super.hideLoading();
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void onContentChanged(int progress) {

    }

    @Override
    public void onScrollChanged(boolean reachedTop, int scroll) {

    }

    @Override
    public void onRefresh() {
        mPresenter.load(true);
    }
}
