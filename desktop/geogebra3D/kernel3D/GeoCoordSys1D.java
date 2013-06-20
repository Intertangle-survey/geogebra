package geogebra3D.kernel3D;

import geogebra.common.kernel.Construction;
import geogebra.common.kernel.Kernel;
import geogebra.common.kernel.MatrixTransformable;
import geogebra.common.kernel.Path;
import geogebra.common.kernel.PathParameter;
import geogebra.common.kernel.Matrix.CoordMatrix;
import geogebra.common.kernel.Matrix.CoordMatrix4x4;
import geogebra.common.kernel.Matrix.CoordMatrixUtil;
import geogebra.common.kernel.Matrix.CoordSys;
import geogebra.common.kernel.Matrix.Coords;
import geogebra.common.kernel.arithmetic.NumberValue;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoPoint;
import geogebra.common.kernel.geos.Traceable;
import geogebra.common.kernel.geos.Translateable;
import geogebra.common.kernel.kernelND.GeoCoordSys;
import geogebra.common.kernel.kernelND.GeoCoordSys1DInterface;
import geogebra.common.kernel.kernelND.GeoDirectionND;
import geogebra.common.kernel.kernelND.GeoLineND;
import geogebra.common.kernel.kernelND.GeoPointND;
import geogebra.common.kernel.kernelND.RotateableND;

import java.util.ArrayList;


public abstract class GeoCoordSys1D extends GeoElement3D implements Path,
GeoLineND, GeoCoordSys, GeoCoordSys1DInterface,
Translateable, MatrixTransformable, 
Traceable, RotateableND {

	protected CoordSys coordsys;
	
	protected GeoPointND startPoint;

	protected GeoPointND endPoint;

	private boolean isIntersection = false;

	public GeoCoordSys1D(Construction c){
		super(c);
		
		// moved from GeoElement's constructor
		// must be called from the subclass, see
		//http://benpryor.com/blog/2008/01/02/dont-call-subclass-methods-from-a-superclass-constructor/
		setConstructionDefaults(); // init visual settings

		coordsys = new CoordSys(1);
	}
	
	public GeoCoordSys1D(Construction c, Coords O, Coords V){
		this(c);
		setCoord(O,V);
	}
	
	
	public GeoCoordSys1D(Construction c, GeoPointND O, GeoPointND I){
		this(c);
		setCoord(O,I);
	}	
	
	
	
	@Override
	public boolean isDefined() {
		return coordsys.isDefined();
	}
	
	

	@Override
	public void setUndefined() {
		coordsys.setUndefined();
	}

	
	
	
	/** set the matrix to [(I-O) O] */
	public void setCoordFromPoints(Coords a_O, Coords a_I){
		 setCoord(a_O,a_I.sub(a_O));
	}
	
	/** set the matrix to [V O] */
	public void setCoord(Coords o, Coords v){
		coordsys.resetCoordSys();
		coordsys.addPoint(o);
		coordsys.addVector(v);
		coordsys.makeOrthoMatrix(false,false);
	}
	
	
	/** set coords to origin O and vector (I-O).
	 * If I (or O) is infinite, I is used as direction vector.
	 * @param O origin point
	 * @param I unit point*/
	public void setCoord(GeoPointND O, GeoPointND I){
		
		startPoint = O;
		endPoint = I;
		
		if ((O==null) || (I==null))
			return;
		
		if (I.isInfinite())
			if (O.isInfinite())
				setUndefined(); //TODO infinite line
			else
				setCoord(O.getInhomCoordsInD(3),I.getCoordsInD(3));
		else
			if (O.isInfinite())
				setCoord(I.getInhomCoordsInD(3),O.getCoordsInD(3));
			else
				setCoord(O.getInhomCoordsInD(3),I.getInhomCoordsInD(3).sub(O.getInhomCoordsInD(3)));
		
	}
	
	
	public void setCoord(GeoCoordSys1D geo){
		setCoord(geo.getCoordSys().getOrigin(),geo.getCoordSys().getVx());
	}
	
	
	@Override
	public void set(GeoElement geo) {
		if (geo instanceof GeoCoordSys1D){
			if (!geo.isDefined())
				setUndefined();
			else
				setCoord((GeoCoordSys1D) geo);
		}else if (geo instanceof GeoLineND){
			if (!geo.isDefined())
				setUndefined();
			else{
				setCoord(((GeoLineND) geo).getStartPoint(), ((GeoLineND) geo).getEndPoint());
			}
		}

	}
	
	
	/**
	 * @param cons 
	 * @return a new instance of the proper GeoCoordSys1D (GeoLine3D, GeoSegment3D, ...)
	 */
	abstract protected GeoCoordSys1D create(Construction cons);
	
	

	@Override
	final public GeoElement copy() {
		GeoCoordSys1D geo = create(cons);
		geo.setCoord(this);
		return geo;
	}
	

	
	/** returns matrix corresponding to segment joining l1 to l2, using getLineThickness() */
	/*
	public GgbMatrix getSegmentMatrix(double l1, double l2){
		
	
		
		return GgbMatrix4x4.subSegmentX(getMatrix4x4(), l1, l2);
	}	
*/
	
	
	/** returns the point at position lambda on the coord sys 
	 * @param lambda 
	 * @return the point at position lambda on the coord sys  */
	public Coords getPoint(double lambda){
		return coordsys.getPoint(lambda);
		
	}


	

	/** returns the point at position lambda on the coord sys in the dimension given
	 * @param dimension 
	 * @param lambda 
	 * @return the point at position lambda on the coord sys  
	 * */
	public Coords getPointInD(int dimension, double lambda){

		Coords v = getPoint(lambda);
		//Application.debug("v("+lambda+")=\n"+v+"\no=\n"+coordsys.getOrigin()+"\nVx=\n"+coordsys.getVx()+"\ncoordsys=\n"+coordsys.getMatrixOrthonormal());
		switch(dimension){
		case 3:
			return v;
		case 2:
			return new Coords(v.getX(), v.getY(), v.getW());
		default:
			return null;
		}
	}


	/** @return cs unit */
	public double getUnit(){

		return getCoordSys().getVx().norm();
	}

	
	@Override
	public Coords getMainDirection(){ 
		return getCoordSys().getMatrixOrthonormal().getVx();
	}

	
	
	
	// Path3D interface
	@Override
	public boolean isPath(){
		return true;
	}
	
	public void pointChanged(GeoPointND P){
		
		
		
		double t = getParamOnLine(P);
		
		if (t<getMinParameter())
			t=getMinParameter();
		else if (t>getMaxParameter())
			t=getMaxParameter();

		
		
		
		
		// set path parameter		
		PathParameter pp = P.getPathParameter();
		
		
		pp.setT(t);
		
		//udpate point using pathChanged
		P.setCoords(getPoint(t),false);
		
		

	}
	//get the param of P's projection on line 
	public double getParamOnLine(GeoPointND P) {
	
		boolean done = false;
		double t = 0;
		if (((GeoElement) P).isGeoElement3D()){
			if (((GeoPoint3D) P).getWillingCoords()!=null){
				if(((GeoPoint3D) P).getWillingDirection()!=null){
					//project willing location using willing direction
					//GgbVector[] project = coordsys.getProjection(P.getWillingCoords(), P.getWillingDirection());

					Coords[] project = ((GeoPoint3D) P).getWillingCoords().projectOnLineWithDirection(
							coordsys.getOrigin(),
							coordsys.getVx(),
							((GeoPoint3D) P).getWillingDirection());

					t = project[1].get(1);
					done = true;
				}else{
					//project current point coordinates
					//Application.debug("ici\n getWillingCoords=\n"+P.getWillingCoords()+"\n matrix=\n"+getMatrix().toString());
					Coords preDirection = ((GeoPoint3D) P).getWillingCoords().sub(coordsys.getOrigin()).crossProduct(coordsys.getVx());
					if(preDirection.equalsForKernel(0, Kernel.STANDARD_PRECISION))
						preDirection = coordsys.getVy();

					Coords[] project = ((GeoPoint3D) P).getWillingCoords().projectOnLineWithDirection(
							coordsys.getOrigin(),
							coordsys.getVx(),
							preDirection.crossProduct(coordsys.getVx()));

					t = project[1].get(1);	
					done = true;
				}
			}
		}
	
		if(!done){
			//project current point coordinates
			//Application.debug("project current point coordinates");
			Coords preDirection = P.getInhomCoordsInD(3).sub(coordsys.getOrigin()).crossProduct(coordsys.getVx());
			if(preDirection.equalsForKernel(0, Kernel.STANDARD_PRECISION))
				preDirection = coordsys.getVy();
		
			Coords[] project = P.getInhomCoordsInD(3).projectOnLineWithDirection(
					coordsys.getOrigin(),
					coordsys.getVx(),
					preDirection.crossProduct(coordsys.getVx()));

			t = project[1].get(1);	
		}
		return t;
	}
	
	
	public void pathChanged(GeoPointND P){
		
		//if kernel doesn't use path/region parameters, do as if point changed its coords
		if(!getKernel().usePathAndRegionParameters(P)){
			pointChanged(P);
			return;
		}
		
		PathParameter pp = P.getPathParameter();
		P.setCoords(getPoint(pp.getT()),false);

	}
	
	
	public boolean isOnPath(GeoPointND PI, double eps){		
		if (PI.getPath()==this)
			return true;
			
		return isOnPath(PI.getCoordsInD(3), eps);
	}
	
	
	public boolean isOnPath(Coords coords, double eps) {    
		return isOnFullLine(coords, eps);		
	}

	
	public boolean isOnFullLine(Coords p, double eps){
		Coords cross;
		
		if (Kernel.isEqual(p.getW(),0,eps))//infinite point : check direction
			cross = p.crossProduct(getDirectionInD3());
		else
			cross = p.sub(getStartInhomCoords()).crossProduct(getDirectionInD3());
		
		
		return cross.equalsForKernel(0,  Kernel.MIN_PRECISION);
	}

	public boolean respectLimitedPath(Coords coords, double eps) {    	
		return true;    	
	} 
	

	////////////////////////////////////
	//
	
	/** return true if x is a valid coordinate (eg 0<=x<=1 for a segment)
	 * @param x coordinate
	 * @return true if x is a valid coordinate (eg 0<=x<=1 for a segment)
	 */
	abstract public boolean isValidCoord(double x);
	
	
	
	////////////////////////////////////
	// XML
	////////////////////////////////////
	
	
    /**
     * returns all class-specific xml tags for saveXML
     */
	@Override
	protected void getXMLtags(StringBuilder sb) {
        super.getXMLtags(sb);
		//	line thickness and type  
		getLineStyleXML(sb);
		
	}
	
	
	

	
	public CoordSys getCoordSys() {
		return coordsys;
	}
	
	
	

	@Override
	public Coords getLabelPosition(){
		return coordsys.getPoint(0.5);
	}

	

	public Coords getCartesianEquationVector(CoordMatrix m){

		if (m==null)
			return CoordMatrixUtil.lineEquationVector(getCoordSys().getOrigin(),getCoordSys().getVx());
		else
			return CoordMatrixUtil.lineEquationVector(getCoordSys().getOrigin(),getCoordSys().getVx(), m);
	}
	
	
	public Coords getStartInhomCoords(){
		return getCoordSys().getOrigin().getInhomCoordsInSameDimension();
	}
	
	/**
	 * @return inhom coords of the end point
	 */
	public Coords getEndInhomCoords(){
		return getCoordSys().getPoint(1).getInhomCoordsInSameDimension();
	}
	
	
	


	public Coords getDirectionInD3(){
		return getCoordSys().getVx();
	}
	
	
	/**
	 * 
	 * @return start point
	 */
	public GeoPointND getStartPoint(){
		return startPoint;
	}
	
	/**
	 * 
	 * @return "end" point
	 */
	public GeoPointND getEndPoint(){
		return endPoint;
	}
	
	
	
	
	public void setIsIntersection(boolean flag) {
		isIntersection = flag;
	}
	
	public boolean isIntersection() {
		return isIntersection;
	}
	
	
	
	
	

	
	@Override
	final public boolean isTranslateable() {
		return true;
	}
	
    final public void translate(Coords v) {        
       coordsys.translate(v);
    }  
    
    
    /////////////////////////////////////
    // POINTS ON COORD SYS
    /////////////////////////////////////
    
    /** list of points on this line*/
	protected ArrayList<GeoPointND> pointsOnLine;

	/**
	 * Returns a list of points that this line passes through. May return null.
	 * 
	 * @return list of points that this line passes through.
	 */
	public final ArrayList<GeoPointND> getPointsOnLine() {
		return pointsOnLine;
	}

	/**
	 * Sets a list of points that this line passes through. This method should
	 * only be used by AlgoMacro.
	 * 
	 * @param points
	 *            list of points that this line passes through
	 */
	public final void setPointsOnLine(ArrayList<GeoPointND> points) {
		pointsOnLine = points;
	}
    
    public final void addPointOnLine(GeoPointND p) {
		if (pointsOnLine == null)
			pointsOnLine = new ArrayList<GeoPointND>();

		if (!pointsOnLine.contains(p))
			pointsOnLine.add(p);
	}

	/**
	 * Calculates the distance between this line and line g.
	 * 
	 * @param g
	 *            line
	 * @return distance between lines
	 */
	final public double distance(GeoLineND g) {
		
		double dist;
		Coords cVector = this.getDirectionInD3().crossProduct(g.getDirectionInD3());
		Coords diffPoints = this.getPointInD(3, 0).sub(g.getPointInD(3, 0));
		
		if (cVector.isZero()) { // two lines are parallel
			Coords n = diffPoints.crossProduct(this.getDirectionInD3()).crossProduct(this.getDirectionInD3());
			dist = Math.abs(diffPoints.dotproduct(n.normalize()));
		} else {
			dist = Math.abs(diffPoints.dotproduct(cVector.normalize()));
		}

		return dist;
	}

	
	
	

	public void setToImplicit() {
		// TODO Auto-generated method stub
		
	}

	public void setToExplicit() {
		// TODO Auto-generated method stub
		
	}

	public void setToParametric(String parameter) {
		// TODO Auto-generated method stub
		
	}
    
	/////////////////////////////
	// MATRIX TRANSFORMABLE
	/////////////////////////////
	
	@Override
	public boolean isMatrixTransformable() {
		return true;
	}
	

	public void matrixTransform(double a00, double a01, double a10, double a11) {
		
		CoordMatrix4x4 m = CoordMatrix4x4.Identity();
		m.set(1,1, a00);
		m.set(1,2, a01);
		m.set(2,1, a10);
		m.set(2,2, a11);
			
		setCoord(m.mul(getCoordSys().getOrigin()), m.mul(getCoordSys().getVx()));
	}


	public void matrixTransform(double a00, double a01, double a02, double a10,
			double a11, double a12, double a20, double a21, double a22) {

		CoordMatrix4x4 m = CoordMatrix4x4.Identity();
		
		m.set(1,1, a00);
		m.set(1,2, a01);		
		m.set(1,3, a02);
		
		
		m.set(2,1, a10);
		m.set(2,2, a11);
		m.set(2,3, a12);
		
		
		m.set(3,1, a20);
		m.set(3,2, a21);		
		m.set(3,3, a22);
		
		
		setCoord(m.mul(getCoordSys().getOrigin()), m.mul(getCoordSys().getVx()));
		
	}

	

	//////////////////
	// TRACE
	//////////////////

	private boolean trace;	
	
	@Override
	public boolean isTraceable() {
		return true;
	}

	public void setTrace(boolean trace) {
		this.trace = trace;
	}

	public boolean getTrace() {
		return trace;
	}
	
	
	////////////////////
	// ROTATE
	////////////////////
	



	public void rotate(NumberValue phiValue) {
		
		Coords o = getCoordSys().getOrigin();
		
		double z = o.getZ();
		/*
		if (!Kernel.isZero(z)){
			setUndefined();
			return;
		}
		*/
	
		Coords v = getCoordSys().getVx();

		double vz = v.getZ();
		/*
		if (!Kernel.isZero(vz)){
			setUndefined();
			return;
		}
		*/

		double phi = phiValue.getDouble();
		double cos = Math.cos(phi);
		double sin = Math.sin(phi);
		
		double x = o.getX();
		double y = o.getY();
		double w = o.getW();


		Coords oRot = new Coords(x * cos - y * sin, x * sin + y * cos, 
				z, w);
		
		double vx = v.getX();
		double vy = v.getY();
		double vw = v.getW();

		
		Coords vRot = new Coords(vx * cos - vy * sin, vx * sin + vy * cos,
				vz, vw);
		
		setCoord(oRot, vRot);
				
	}
	
	final public void rotate(NumberValue phiValue, GeoPoint Q) {

		Coords o = getCoordSys().getOrigin();

		double z = o.getZ();
		/*
		if (!Kernel.isZero(z)){
			setUndefined();
			return;
		}
		*/
	
		Coords v = getCoordSys().getVx();

		double vz = v.getZ();
		/*
		if (!Kernel.isZero(vz)){
			setUndefined();
			return;
		}
		*/

		double phi = phiValue.getDouble();
		double cos = Math.cos(phi);
		double sin = Math.sin(phi);
		
		double x = o.getX();
		double y = o.getY();
		double w = o.getW();

		double qx = w * Q.getInhomX();
		double qy = w * Q.getInhomY();

		Coords oRot = new Coords((x - qx) * cos + (qy - y) * sin + qx, (x - qx) * sin
				+ (y - qy) * cos + qy, z, w);
		
		double vx = v.getX();
		double vy = v.getY();
		double vw = v.getW();

		
		Coords vRot = new Coords(vx * cos - vy * sin, vx * sin + vy * cos,
				vz, vw);
		
		setCoord(oRot, vRot);
		
		
	}
	
	final private void rotate(NumberValue phiValue, Coords o1, Coords vn) {

		if (vn.isZero()){
			setUndefined();
			return;
		}
		Coords vn2 = vn.normalized();
		
		
		Coords point = getCoordSys().getOrigin();
		Coords s = point.projectLine(o1, vn)[0]; //point projected on the axis
		
		Coords v1 = point.sub(s); //axis->point of the line

		
		Coords v = getCoordSys().getVx(); //direction of the line
		

		double phi = phiValue.getDouble();
		double cos = Math.cos(phi);
		double sin = Math.sin(phi);
		
		// new line origin
		Coords v2 = vn2.crossProduct4(v1);
		Coords oRot = s.add(v1.mul(cos)).add(v2.mul(sin));
		
		// new line direction
		v2 = vn2.crossProduct4(v);
		v1 = v2.crossProduct4(vn2);
		Coords vRot = v1.mul(cos).add(v2.mul(sin)).add(vn2.mul(v.dotproduct(vn2)));


		setCoord(oRot, vRot);
	}
	
	

	public void rotate(NumberValue phiValue, GeoPointND S, GeoDirectionND orientation) {
		
		Coords o1 = S.getInhomCoordsInD(3);
		
		Coords vn = orientation.getDirectionInD3();

		rotate(phiValue, o1, vn);
	}

	public void rotate(NumberValue phiValue, GeoLineND line) {
		
		Coords o1 = line.getStartInhomCoords();
		Coords vn = line.getDirectionInD3();
		

		rotate(phiValue, o1, vn);
		
	}
	
}
