package com.verification.inheritance;

class GrandParent {
    public void grandParentMethod() {}
}

class Parent extends GrandParent {
    public void parentMethod() {}
}

public class DIT_TestClass extends Parent {
    public void ownMethod() {}
}

// Test case for deeper inheritance
class Level1 {}
class Level2 extends Level1 {}
class Level3 extends Level2 {}
class Level4 extends Level3 {}

public class DeepInheritance_TestClass extends Level4 {
    public void deepMethod() {}
}

// Test case for Object inheritance only
public class ShallowInheritance_TestClass {
    public void shallowMethod() {}
}