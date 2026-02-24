package com.example.demo;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

class ServiceTest {

    @Test
    void testInterfaceWithoutImplementation() {

        I mockObj = mock(I.class);
        Service service = new Service(mockObj);
        service.perform();
        verify(mockObj, times(3)).abc();
    }
}