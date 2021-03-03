/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.Drawing;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.components.drawing.course.CoordinateBasedDrawingMethod;
import org.opentcs.guing.components.drawing.course.DrawingMethod;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.exchange.EventDispatcher;
import static org.opentcs.guing.model.ModelComponent.NAME;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.OtherGraphicalElement;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.CourseObjectFactory;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Standardimplementierung des Datenmodells des gesamten modellierten Systems.
 * Besteht aus den Batterien, den Transportgütern, den Fahrzeugtypen mit ihren
 * Fahrzeugen und dem Fahrkurslayout. Das Systemmodell verwaltet zusätzlich zu
 * seinen Kindelementen eine Hastable mit den Komposita-Komponenten, die
 * unbedingt vorhanden sein müssen. Die Applikation als Klient fragt das
 * Systemmodell dann nach einer bestimmten Komponente (z.B. der für die
 * Transportgüter), um herauszufinden, wo ein neu erzeugtes Transportgut
 * abgelegt werden kann.
 * <p>
 * <b>Entwurfsmuster:</b> Kompositum.
 * StandardSystemModel ist ein konkretes Kompositum.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
class StandardSystemModel
    extends CompositeModelComponent
    implements SystemModel {

  /**
   * Die Hashtable mit Zuordnungen zwischen Strings und den Hauptkomponenten des
   * Modells.
   */
  private final Map<FolderKey, ModelComponent> fMainFolders = new HashMap<>();
  /**
   * Enthält Zuordnungen zwischen den Hauptordnern des Modells und
   * Class-Objekten von ModelComponent-Objekten. Hierdurch ist praktisch
   * konfigurierbar, welche ModelComponent-Objekte in welchen Ordner gehören.
   */
  private final Map<Class, ModelComponent> fParentFolders = new HashMap<>();
  /**
   * Die Zeichnung.
   */
  private final Drawing fDrawing = new DefaultDrawing();
  /**
   * Die Map mit der Zuordnung der Model-Layout-Elemente zu den Kernel-Objekten
   */
  private final Map<TCSObjectReference<?>, ModelLayoutElement> fLayoutMap
      = new HashMap<>();
  /**
   * Die für das Modell verwendete Zeichenmethode.
   */
  private final DrawingMethod fDrawingMethod;
  /**
   * Die Tabelle mit den Zuordnungen zwischen Modellkomponente und
   * Leitsteuerungsobjekt.
   */
  private final EventDispatcher fEventDispatcher;

  private final CourseObjectFactory crsObjFactory;

  /**
   * Creates a new instance.
   *
   * @param drawingMethod The drawing method.
   */
  public StandardSystemModel(DrawingMethod drawingMethod,
                             EventDispatcher eventDispatcher,
                             CourseObjectFactory crsObjFactory) {
    super("Model");
    this.fDrawingMethod = requireNonNull(drawingMethod, "drawingMethod");
    this.fEventDispatcher = requireNonNull(eventDispatcher, "eventDispatcher");
    this.crsObjFactory = requireNonNull(crsObjFactory, "crsObjFactory");

    createMainFolders();
    setupParentFolders();
    createProperties();
  }

  /**
   * Creates a new instance with a default drawing method.
   */
  @Inject
  public StandardSystemModel(EventDispatcher eventDispatcher,
                             CourseObjectFactory crsObjFactory) {
    this(new CoordinateBasedDrawingMethod(), eventDispatcher, crsObjFactory);
  }

  @Override // SystemModel
  public void addMainFolder(FolderKey key, ModelComponent component) {
    fMainFolders.put(key, component);
  }

  @Override // SystemModel
  public ModelComponent getMainFolder(FolderKey key) {
    return fMainFolders.get(key);
  }

  @Override // SystemModel
  public ModelComponent getFolder(ModelComponent item) {
    if (item == null) {
      return null;
    }

    for (Class c : fParentFolders.keySet()) {
      if (item.getClass() == c || c.isInstance(item)) {
        return fParentFolders.get(c);
      }
    }

    return null;
  }

  @Override // SystemModel
  public <T> List<T> getAll(FolderKey foldername, Class<T> classType) {
    List<T> items = new ArrayList<>();
    for (ModelComponent o : getMainFolder(foldername).getChildComponents()) {
      if (classType.isInstance(o)) {
        items.add(classType.cast(o));
      }
    }

    return items;
  }

  @Override
  public List<ModelComponent> getAll() {
    List<ModelComponent> items = new ArrayList<>();
    for (ModelComponent o : fMainFolders.values()) { //Iterate over folders
      if (o instanceof CompositeModelComponent) {
        items.addAll(getAll((CompositeModelComponent) o));
      }
      else {
        items.add(o);
      }
    }
    return items;
  }

  @Override // SystemModel
  public EventDispatcher getEventDispatcher() {
    return fEventDispatcher;
  }

  @Override // SystemModel
  public Drawing getDrawing() {
    return fDrawing;
  }

  @Override // SystemModel
  public DrawingMethod getDrawingMethod() {
    return fDrawingMethod;
  }

  @Override // SystemModel
  public List<VehicleModel> getVehicleModels() {
    List<VehicleModel> vehicles = new ArrayList<>();

    for (ModelComponent vComp : getMainFolder(FolderKey.VEHICLES).getChildComponents()) {
      vehicles.add((VehicleModel) vComp);
    }

    return vehicles;
  }

  @Override // SystemModel
  public VehicleModel getVehicleModel(String name) {
    for (VehicleModel vehicle : getVehicleModels()) {
      if (vehicle.getName().equals(name)) {
        return vehicle;
      }
    }

    return null;
  }

  @Override // SystemModel
  public List<LayoutModel> getLayoutModels() {
    List<LayoutModel> layouts = new ArrayList<>();
    // TODO: Erweitern, wenn es mal mehrere Visual Layouts zu einem Kernel-Modell geben sollte
    LayoutModel item = (LayoutModel) getMainFolder(FolderKey.LAYOUT);
    layouts.add(item);

    return layouts;
  }

  @Override // SystemModel
  public LayoutModel getLayoutModel(String name) {
    // TODO: Erweitern, wenn es mal mehrere Visual Layouts zu einem Kernel-Modell geben sollte
    return null;
  }

  @Override // SystemModel
  public List<PointModel> getPointModels() {
    return getAll(FolderKey.POINTS, PointModel.class);
  }

  @Override // SystemModel
  public PointModel getPointModel(String name) {
    for (PointModel point : getPointModels()) {
      if (point.getName().equals(name)) {
        return point;
      }
    }

    return null;
  }

  @Override // SystemModel
  public List<LocationModel> getLocationModels() {
    List<LocationModel> items = new ArrayList<>();

    for (ModelComponent item : getMainFolder(FolderKey.LOCATIONS).getChildComponents()) {
      if (item instanceof LocationModel) {
        items.add((LocationModel) item);
      }
    }

    return items;
  }

  @Override // SystemModel
  public List<LocationModel> getLocationModels(LocationTypeModel type) {
    List<LocationModel> items = new ArrayList<>();
    Iterator<LocationModel> e = getLocationModels().iterator();

    while (e.hasNext()) {
      LocationModel s = e.next();

      if (s.getLocationType() == type) {
        items.add(s);
      }
    }

    return items;
  }

  @Override // SystemModel
  public LocationModel getLocationModel(String name) {
    for (LocationModel location : getLocationModels()) {
      if (location.getName().equals(name)) {
        return location;
      }
    }

    return null;
  }

  @Override // SystemModel
  public List<PathModel> getPathModels() {
    List<PathModel> items = new ArrayList<>();

    for (ModelComponent item : getMainFolder(FolderKey.PATHS).getChildComponents()) {
      // XXX Why is this here? LinkModel is not a subclass of PathModel...
      if (item instanceof LinkModel) {
        continue;
      }
      if (item instanceof PathModel) {
        items.add((PathModel) item);
      }
    }

    return items;
  }

  @Override
  public PathModel getPathModel(String name) {
    for (PathModel path : getPathModels()) {
      if (path.getName().equals(name)) {
        return path;
      }
    }

    return null;
  }

  @Override // SystemModel
  public List<LinkModel> getLinkModels() {
    List<LinkModel> items = new ArrayList<>();

    for (ModelComponent item : getMainFolder(FolderKey.LINKS).getChildComponents()) {
      if (item instanceof LinkModel) {
        items.add((LinkModel) item);
      }
    }

    return items;
  }

  @Override // SystemModel
  public List<LinkModel> getLinkModels(LocationTypeModel locationType) {
    List<LinkModel> items = new ArrayList<>();

    for (LinkModel ref : getAll(FolderKey.LINKS, LinkModel.class)) {
      if (ref.getLocation().getLocationType() == locationType) {
        items.add(ref);
      }
    }

    return items;
  }

  @Override // SystemModel
  public List<LocationTypeModel> getLocationTypeModels() {
    List<LocationTypeModel> result = new ArrayList<>();
    for (ModelComponent component
             : getMainFolder(FolderKey.LOCATION_TYPES).getChildComponents()) {
      result.add((LocationTypeModel) component);
    }
    return result;
  }

  @Override // SystemModel
  public LocationTypeModel getLocationTypeModel(String name) {
    for (LocationTypeModel t : getLocationTypeModels()) {
      if (t.getName().equals(name)) {
        return t;
      }
    }

    return null;
  }

  @Override // SystemModel
  public List<BlockModel> getBlockModels() {
    List<BlockModel> result = new ArrayList<>();
    for (ModelComponent component : getMainFolder(FolderKey.BLOCKS).getChildComponents()) {
      result.add((BlockModel) component);
    }
    return result;
  }

  @Override // SystemModel
  public List<GroupModel> getGroupModels() {
    List<GroupModel> result = new ArrayList<>();
    for (ModelComponent component : getMainFolder(FolderKey.GROUPS).getChildComponents()) {
      result.add((GroupModel) component);
    }
    return result;
  }

  @Override // SystemModel
  public List<StaticRouteModel> getStaticRouteModels() {
    List<StaticRouteModel> result = new ArrayList<>();
    for (ModelComponent component
             : getMainFolder(FolderKey.STATIC_ROUTES).getChildComponents()) {
      result.add((StaticRouteModel) component);
    }
    return result;
  }

  @Override // SystemModel
  public List<OtherGraphicalElement> getOtherGraphicalElements() {
    List<OtherGraphicalElement> result = new ArrayList<>();
    for (ModelComponent component
             : getMainFolder(FolderKey.OTHER_GRAPHICAL_ELEMENTS).getChildComponents()) {
      result.add((OtherGraphicalElement) component);
    }
    return result;
  }

  @Override
  public void createLayoutMap(VisualLayout layout,
                              Set<Point> points,
                              Set<Path> paths,
                              Set<Location> locations,
                              Set<Block> blocks) {

    Set<LayoutElement> elements = layout.getLayoutElements();

    for (Point point : points) {
      mapLayoutElement(point.getReference(), elements);
    }

    for (Path path : paths) {
      mapLayoutElement(path.getReference(), elements);
    }

    for (Location location : locations) {
      mapLayoutElement(location.getReference(), elements);
    }

    for (Block block : blocks) {
      mapLayoutElement(block.getReference(), elements);
    }
  }

  @Override
  public Map<TCSObjectReference<?>, ModelLayoutElement> getLayoutMap() {
    return fLayoutMap;
  }

  /**
   * Liefert rekursiv alle Komponenten in dem Ordner.
   *
   * @param folder der Ordner
   * @return alle Elemente in dem Ordner
   */
  private List<ModelComponent> getAll(CompositeModelComponent folder) {
    List<ModelComponent> result = new LinkedList<>();
    for (ModelComponent component : folder.getChildComponents()) {
      if (component instanceof CompositeModelComponent) {
        result.addAll(getAll((CompositeModelComponent) component));
      }
      else {
        result.add(component);
      }
    }
    return result;
  }

  private void mapLayoutElement(TCSObjectReference<?> reference,
                                Collection<LayoutElement> elements) {
    for (LayoutElement element : elements) {
      ModelLayoutElement mle = (ModelLayoutElement) element;

      if (Objects.equals(mle.getVisualizedObject(), reference)) {
        fLayoutMap.put(reference, mle);
        break;
      }
    }
  }

  /**
   * Erstellt die unveränderlichen Hauptordner des TreeViews. Hauptordner
   * existieren immer, auch wenn es sonst keine Komponenten im Systemmodell
   * gibt. Hauptordner sollen allein durch Nutzereingaben nicht gelöscht werden
   * können.
   */
  private void createMainFolders() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    createMainFolder(this, FolderKey.VEHICLES,
                     new SimpleFolder(bundle.getString("tree.vehicles.text")));

    LayoutModel layoutModel = crsObjFactory.createLayoutModel();
    layoutModel.setName("VLayout-1");
    createMainFolder(this, FolderKey.LAYOUT, layoutModel);

    createMainFolder(getMainFolder(FolderKey.LAYOUT), FolderKey.POINTS,
                     new SimpleFolder(bundle.getString("tree.points.text")));
    createMainFolder(getMainFolder(FolderKey.LAYOUT), FolderKey.PATHS,
                     new SimpleFolder(bundle.getString("tree.paths.text")));
    createMainFolder(getMainFolder(FolderKey.LAYOUT), FolderKey.LOCATIONS,
                     new SimpleFolder(bundle.getString("tree.locations.text")));
    createMainFolder(getMainFolder(FolderKey.LAYOUT), FolderKey.LOCATION_TYPES,
                     new SimpleFolder(bundle.getString("tree.locationTypes.text")));
    createMainFolder(getMainFolder(FolderKey.LAYOUT), FolderKey.LINKS,
                     new SimpleFolder(bundle.getString("tree.links.text")));
    createMainFolder(getMainFolder(FolderKey.LAYOUT), FolderKey.BLOCKS,
                     new SimpleFolder(bundle.getString("tree.blocks.text")));
    createMainFolder(getMainFolder(FolderKey.LAYOUT), FolderKey.GROUPS,
                     new SimpleFolder(bundle.getString("tree.groups.text")));
    createMainFolder(getMainFolder(FolderKey.LAYOUT), FolderKey.STATIC_ROUTES,
                     new SimpleFolder(bundle.getString("tree.staticRoutes.text")));
    createMainFolder(getMainFolder(FolderKey.LAYOUT), FolderKey.OTHER_GRAPHICAL_ELEMENTS,
                     new SimpleFolder(bundle.getString("tree.otherGraphicals.text")));
  }

  private void createProperties() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    // Name
    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("systemModel.name.text"));
    pName.setHelptext(bundle.getString("systemModel.name.helptext"));
    setProperty(NAME, pName);
  }

  /**
   * Erzeugt einen Hauptordner, der sowohl dem TreeView als auch dem
   * Systemmodell hinzugefügt wird.
   */
  private void createMainFolder(ModelComponent parentFolder,
                                FolderKey key,
                                ModelComponent newFolder) {
    addMainFolder(key, newFolder);
    parentFolder.add(newFolder);
  }

  /**
   * Initialisiert die Zuordnungen zwischen ModelComponent-Ordnern und
   * ModelComponent-Inhalten anhand von Class-Objekten.
   */
  private void setupParentFolders() {
    fParentFolders.put(VehicleModel.class, getMainFolder(FolderKey.VEHICLES));
    fParentFolders.put(LayoutModel.class, getMainFolder(FolderKey.LAYOUT));
    fParentFolders.put(PointModel.class, getMainFolder(FolderKey.POINTS));
    fParentFolders.put(PathModel.class, getMainFolder(FolderKey.PATHS));
    fParentFolders.put(LocationModel.class, getMainFolder(FolderKey.LOCATIONS));
    fParentFolders.put(LocationTypeModel.class, getMainFolder(FolderKey.LOCATION_TYPES));
    fParentFolders.put(LinkModel.class, getMainFolder(FolderKey.LINKS));
    fParentFolders.put(BlockModel.class, getMainFolder(FolderKey.BLOCKS));
    fParentFolders.put(GroupModel.class, getMainFolder(FolderKey.GROUPS));
    fParentFolders.put(StaticRouteModel.class, getMainFolder(FolderKey.STATIC_ROUTES));
    fParentFolders.put(OtherGraphicalElement.class, getMainFolder(FolderKey.OTHER_GRAPHICAL_ELEMENTS));
  }
}
