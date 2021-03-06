/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.uberfire.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.google.common.collect.Sets;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.uberfire.client.menu.CustomSplashHelp;
import org.uberfire.client.menu.WorkbenchViewModeSwitcherMenuBuilder;
import org.uberfire.client.mvp.ActivityManager;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.WorkbenchScreenActivity;
import org.uberfire.client.navbar.SearchMenuBuilder;
import org.uberfire.client.perspectives.SimplePerspectiveNoContext;
import org.uberfire.client.resources.AppResource;
import org.uberfire.client.screen.JSWorkbenchScreenActivity;
import org.uberfire.client.views.pfly.menu.MainBrand;
import org.uberfire.client.views.pfly.menu.UserMenu;
import org.uberfire.client.workbench.events.ApplicationReadyEvent;
import org.uberfire.client.workbench.widgets.menu.UtilityMenuBar;
import org.uberfire.client.workbench.widgets.menu.WorkbenchMenuBar;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

import static org.uberfire.workbench.model.menu.MenuFactory.*;

/**
 * GWT's Entry-point for Uberfire-showcase
 */
@EntryPoint
public class ShowcaseEntryPoint {

    @Inject
    private SyncBeanManager manager;

    @Inject
    private WorkbenchMenuBar menubar;

    @Inject
    private UserMenu userMenu;

    @Inject
    private User user;

    @Inject
    private UtilityMenuBar utilityMenu;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private ActivityManager activityManager;

    @Inject
    private Caller<AuthenticationService> authService;

    @Inject
    private SearchMenuBuilder searchMenuBuilder;

    private static final Set<String> menuItemsToRemove = Sets.newHashSet(
            "IFrameScreen",
            "IPInfoGadget",
            "SportsNewsGadget",
            "StockQuotesGadget",
            "WeatherGadget",
            "YouTubeScreen",
            "YouTubeVideos",
            "chartPopulator",
            "welcome"
    );

    @AfterInitialization
    public void startApp() {
        hideLoadingPopup();
    }

    private void setupMenu( @Observes final ApplicationReadyEvent event ) {
        final PerspectiveActivity defaultPerspective = getDefaultPerspectiveActivity();

        final Menus menus =
                         newTopLevelMenu( "Home" )
                            .perspective( defaultPerspective.getIdentifier() )
                        .endMenu()
                        .newTopLevelMenu( "Perspectives" )
                            .withItems( getPerspectives() )
                        .endMenu()
                        .newTopLevelMenu( "Screens" )
                            .withItems( getScreens() )
                        .endMenu()
                        .newTopLevelCustomMenu( searchMenuBuilder )
                        .endMenu()
                        .build();

        menubar.addMenus( menus );

        userMenu.addMenus(
                newTopLevelMenu( "Logout" ).respondsWith( () -> {
                    authService.call().logout();
                } ).endMenu()
                        .newTopLevelMenu( "My roles" ).respondsWith(() -> {
                            final Set<Role> roles = user.getRoles();
                            if ( roles == null || roles.isEmpty() ) {
                                Window.alert( "You have no roles assigned" );
                            } else {
                                Window.alert( "Currently logged in using roles: " + roles );
                            }
                        } )
                        .endMenu()
                        .newTopLevelCustomMenu( manager.lookupBean( WorkbenchViewModeSwitcherMenuBuilder.class ).getInstance() )
                        .endMenu()
                        .build() );

        utilityMenu.addMenus(
                newTopLevelCustomMenu( userMenu ).endMenu()
                        .newTopLevelMenu( "Status" )
                        .identifier( "usermenu.status" )
                        .respondsWith( () -> {
                            Window.alert( "Hello from status!" );
                        } )
                .endMenu()
                .newTopLevelCustomMenu( manager.lookupBean( CustomSplashHelp.class ).getInstance() )
                .endMenu()
                .build() );
    }

    public static List<MenuItem> getScreens() {
        final List<MenuItem> screens = new ArrayList<MenuItem>();
        final List<String> names = new ArrayList<String>();

        for ( final IOCBeanDef<WorkbenchScreenActivity> _menuItem : IOC.getBeanManager().lookupBeans( WorkbenchScreenActivity.class ) ) {
            final String name;
            if ( _menuItem.getBeanClass().equals( JSWorkbenchScreenActivity.class ) ) {
                name = _menuItem.getName();
            } else {
                name = IOC.getBeanManager().lookupBean( _menuItem.getBeanClass() ).getName();
            }

            if ( !menuItemsToRemove.contains( name ) ) {
                names.add( name );
            }
        }

        Collections.sort( names );

        final PlaceManager placeManager = IOC.getBeanManager().lookupBean( PlaceManager.class ).getInstance();
        for ( final String name : names ) {
            final MenuItem item = MenuFactory.newSimpleItem( name )
                    .identifier( "screen.read." + name )
                    .respondsWith( () -> {
                        placeManager.goTo( new DefaultPlaceRequest( name ) );
                    } ).endMenu().build().getItems().get( 0 );
            screens.add( item );
        }

        return screens;
    }

    private List<MenuItem> getPerspectives() {
        final List<MenuItem> perspectives = new ArrayList<MenuItem>();
        for ( final PerspectiveActivity perspective : getPerspectiveActivities() ) {
            if (SimplePerspectiveNoContext.SIMPLE_PERSPECTIVE_NO_CONTEXT.equals(perspective.getIdentifier())) {
                continue;
            }
            final String name = perspective.getDefaultPerspectiveLayout().getName();
            final MenuItem item = MenuFactory.newSimpleItem( name ).perspective( perspective.getIdentifier() ).endMenu().build().getItems().get( 0 );
            perspectives.add( item );
        }

        return perspectives;
    }

    private PerspectiveActivity getDefaultPerspectiveActivity() {
        PerspectiveActivity defaultPerspective = null;
        final Collection<SyncBeanDef<PerspectiveActivity>> perspectives = manager.lookupBeans( PerspectiveActivity.class );
        final Iterator<SyncBeanDef<PerspectiveActivity>> perspectivesIterator = perspectives.iterator();

        while ( perspectivesIterator.hasNext() ) {
            final SyncBeanDef<PerspectiveActivity> perspective = perspectivesIterator.next();
            final PerspectiveActivity instance = perspective.getInstance();
            if ( instance.isDefault() ) {
                defaultPerspective = instance;
                break;
            } else {
                manager.destroyBean( instance );
            }
        }
        return defaultPerspective;
    }

    private List<PerspectiveActivity> getPerspectiveActivities() {

        //Get Perspective Providers
        final Set<PerspectiveActivity> activities = activityManager.getActivities( PerspectiveActivity.class );

        //Remove default perspective to avoid duplicate menu
        final Iterator<PerspectiveActivity> iterator = activities.iterator();
        while( iterator.hasNext() ){
            final PerspectiveActivity activity = iterator.next();
            if( activity.isDefault() ){
                iterator.remove();
            }
        }

        //Sort Perspective Providers so they're always in the same sequence!
        List<PerspectiveActivity> sortedActivities = new ArrayList<PerspectiveActivity>( activities );
        Collections.sort( sortedActivities,
                new Comparator<PerspectiveActivity>() {

                    @Override
                    public int compare( PerspectiveActivity o1,
                                        PerspectiveActivity o2 ) {
                        return o1.getDefaultPerspectiveLayout().getName().compareTo( o2.getDefaultPerspectiveLayout().getName() );
                    }

                } );

        return sortedActivities;
    }

    private Collection<WorkbenchScreenActivity> getScreenActivities() {

        //Get Perspective Providers
        return activityManager.getActivities( WorkbenchScreenActivity.class );
    }

    //Fade out the "Loading application" pop-up
    private void hideLoadingPopup() {
        final Element e = RootPanel.get( "loading" ).getElement();

        new Animation() {

            @Override
            protected void onUpdate( double progress ) {
                e.getStyle().setOpacity( 1.0 - progress );
            }

            @Override
            protected void onComplete() {
                e.getStyle().setVisibility( Style.Visibility.HIDDEN );
            }
        }.run( 500 );
    }

    @Produces
    @ApplicationScoped
    public MainBrand createBrandLogo() {
        return new MainBrand() {
            @Override
            public Widget asWidget() {
                return new Image( AppResource.INSTANCE.images().ufBrandLogo() );
            }
        };
    }

}
